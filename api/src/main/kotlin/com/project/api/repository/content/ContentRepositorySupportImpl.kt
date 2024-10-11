package com.project.api.repository.content

import com.project.api.web.dto.response.ContentSearchResponse
import com.project.api.web.dto.response.ContentSearchResponse.Companion.toContentSearchResponse
import com.project.core.domain.content.QContent
import com.project.core.domain.content.QContentLanguage
import com.project.core.domain.content.QContentSkill
import com.project.core.domain.user.User
import com.project.core.internal.ContentType
import com.project.core.util.QueryDslUtil
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable

class ContentRepositorySupportImpl(
    private val query: JPAQueryFactory,
) : ContentRepositorySupport {
    override fun search(
        user: User,
        name: String?,
        groupId: Long?,
        sectionId: Long?,
        languages: List<String>?,
        skills: List<String>?,
        writer: String?,
        startDate: Long?,
        endDate: Long?,
        pageable: Pageable,
        type: ContentType?,
    ): List<ContentSearchResponse> {
        val builder =
            createBuilder(
                groupId = groupId,
                sectionId = sectionId,
                name = name,
                languages = languages,
                skills = skills,
                writer = writer,
                startDate = startDate,
                endDate = endDate,
                type = type,
            )
        val queryBuilder =
            query
                .select(
                    QContent.content1,
                ).from(QContent.content1)

        if (!languages.isNullOrEmpty()) {
            queryBuilder.join(QContent.content1.skills, QContentSkill.contentSkill)
        }

        if (!skills.isNullOrEmpty()) {
            queryBuilder.join(QContent.content1.languages, QContentLanguage.contentLanguage)
        }

        queryBuilder
            .where(builder)
            .groupBy(QContent.content1.id)
        queryBuilder.orderBy(*QueryDslUtil.orders(pageable, QContent.content1::class.java, "content1"))

        val results =
            queryBuilder
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        return results.map { it.toContentSearchResponse() }
    }

    private fun createBuilder(
        groupId: Long?,
        sectionId: Long?,
        name: String?,
        languages: List<String>?,
        skills: List<String>?,
        writer: String?,
        startDate: Long?,
        endDate: Long?,
        type: ContentType?,
    ): BooleanBuilder {
        val builder = BooleanBuilder()

        groupId?.let {
            builder.and(
                QContent.content1.groupUser.group.id
                    .eq(groupId),
            )
        } ?: run {
            builder.and(QContent.content1.groupUser.group.isPublic.isTrue)
        }
        sectionId?.let {
            builder.and(
                QContent.content1.section.id
                    .eq(sectionId),
            )
        }
        name?.let { builder.and(QContent.content1.name.containsIgnoreCase(it)) }
        languages?.let { builder.and(QContentLanguage.contentLanguage.name.`in`(it)) }
        skills?.let { builder.and(QContentSkill.contentSkill.name.`in`(it)) }
        writer?.let {
            builder.and(
                QContent.content1.groupUser.name
                    .containsIgnoreCase(it),
            )
        }
        startDate?.let { start ->
            endDate?.let { end ->
                builder.and(QContent.content1.createdDate.between(start, end))
            }
        }
        type?.let {
            builder.and(
                QContent.content1.type.eq(it),
            )
        }

        if (type != null) {
            builder.and(
                QContent.content1.type.eq(type),
            )
        } else {
            builder.and(
                !QContent.content1.type.eq(ContentType.FILE),
            )
        }
        return builder
    }
}
