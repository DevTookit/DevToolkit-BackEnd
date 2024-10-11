package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.bookmark.BookmarkRepository
import com.project.api.repository.category.SectionRepository
import com.project.api.repository.content.ContentRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.BookmarkCreateRequest
import com.project.api.web.dto.response.BookmarkCreateResponse
import com.project.api.web.dto.response.BookmarkCreateResponse.Companion.toBookmarkCreateResponse
import com.project.api.web.dto.response.BookmarkResponse
import com.project.api.web.dto.response.BookmarkResponse.Companion.toBookmarkResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.bookmark.Bookmark
import com.project.core.domain.bookmark.QBookmark
import com.project.core.internal.BookmarkType
import com.project.core.internal.ContentType
import com.querydsl.core.BooleanBuilder
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository,
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val userRepository: UserRepository,
    private val contentRepository: ContentRepository,
    private val sectionRepository: SectionRepository,
) {
    fun readAll(
        email: String,
        groupId: Long,
        type: BookmarkType?,
        pageable: Pageable,
    ): List<BookmarkResponse> {
        val userInfo = validate(email, groupId)
        return bookmarkRepository
            .findAll(
                BooleanBuilder()
                    .and(QBookmark.bookmark.user.eq(userInfo.user))
                    .and(QBookmark.bookmark.group.eq(userInfo.group))
                    .and(type?.let { QBookmark.bookmark.type.eq(it) }),
                pageable,
            ).content
            .map {
                toResponse(it)
            }
    }

    @Transactional
    fun create(
        email: String,
        request: BookmarkCreateRequest,
    ): BookmarkCreateResponse {
        val userInfo = validate(email, request.groupId)

        val contentId = validateBookmark(request.type, request.contentId)
        val section =
            sectionRepository.findByIdOrNull(request.sectionId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_SECTION.message)
        return bookmarkRepository
            .save(
                Bookmark(
                    contentId = contentId,
                    user = userInfo.user,
                    group = userInfo.group,
                    type = request.type,
                    section = section,
                ),
            ).toBookmarkCreateResponse()
    }

    @Transactional
    fun delete(
        email: String,
        groupId: Long,
        bookmarkId: Long,
    ) {
        val userInfo = validate(email, groupId)
        val bookmark =
            bookmarkRepository.findByIdAndUser(bookmarkId, userInfo.user)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_FOLDER.message)
        bookmarkRepository.delete(bookmark)
    }

    private fun toResponse(bookmark: Bookmark): BookmarkResponse {
        val content =
            contentRepository.findByIdAndType(bookmark.contentId, ContentType.valueOf(bookmark.type.name))
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_CONTENT.message)
        return content.toBookmarkResponse(bookmark.id)
    }

    private fun validateBookmark(
        type: BookmarkType,
        contentId: Long,
    ): Long {
        val content =
            contentRepository.findByIdAndType(contentId, ContentType.valueOf(type.name))
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_CONTENT.message)
        return content.id!!
    }

    private fun validate(
        email: String,
        groupId: Long,
    ): UserValidateResponse {
        val user =
            userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        val groupUser = (
            groupUserRepository.findByUserAndGroup(user, group)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)
        )

        if (!groupUser.role.isActive()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return UserValidateResponse(
            user = user,
            group = group,
            groupUser = groupUser,
        )
    }
}
