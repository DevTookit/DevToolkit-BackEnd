package com.project.api.repository.content

import com.project.core.domain.content.Content
import com.project.core.domain.group.GroupUser
import com.project.core.domain.section.Section
import com.project.core.internal.ContentType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface ContentRepository :
    JpaRepository<Content, Long>,
    QuerydslPredicateExecutor<Content>,
    ContentRepositorySupport {
    fun findByIdAndType(
        id: Long,
        type: ContentType,
    ): Content?

    fun findByIdAndTypeAndGroupUser(
        id: Long,
        type: ContentType,
        user: GroupUser,
    ): Content?

    @EntityGraph(attributePaths = ["groupUser"])
    fun findByIdAndSection(
        id: Long,
        section: Section,
    ): Content?

    @EntityGraph(attributePaths = ["groupUser"])
    fun findByIdAndSectionAndType(
        id: Long,
        section: Section,
        type: ContentType,
    ): Content?

    fun findAllByGroupIsPublicTrueOrderByVisitCntDesc(pageable: Pageable): List<Content>

    fun findAllBySectionIsPublicTrueOrderByVisitCntDesc(pageable: Pageable): List<Content>

    @EntityGraph(attributePaths = ["groupUser"])
    fun findByParentFolder(
        parent: Content,
        pageable: Pageable,
    ): List<Content>

    fun deleteByIdAndSection(
        id: Long,
        section: Section,
    )
}
