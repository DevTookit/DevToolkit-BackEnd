package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.announcement.AnnouncementRepository
import com.project.api.repository.comment.CommentMentionRepository
import com.project.api.repository.comment.CommentRepository
import com.project.api.repository.content.ContentRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.CommentCreateRequest
import com.project.api.web.dto.request.CommentUpdateRequest
import com.project.api.web.dto.response.CommentCreateResponse
import com.project.api.web.dto.response.CommentCreateResponse.Companion.toCommentCreateResponse
import com.project.api.web.dto.response.CommentReadResponse
import com.project.api.web.dto.response.CommentReadResponse.Companion.toCommentReadResponse
import com.project.api.web.dto.response.CommentUpdateResponse
import com.project.api.web.dto.response.CommentUpdateResponse.Companion.toCommentUpdateResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.comment.Comment
import com.project.core.domain.comment.CommentMention
import com.project.core.internal.CommentType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val contentRepository: ContentRepository,
    private val commentRepository: CommentRepository,
    private val commentMentionRepository: CommentMentionRepository,
    private val groupUserRepository: GroupUserRepository,
    private val announcementRepository: AnnouncementRepository,
) {
    @Transactional(readOnly = true)
    fun readAll(
        email: String,
        groupId: Long,
        contentId: Long,
    ): List<CommentReadResponse> {
        val userResponse = validatePublic(email, groupId)
        val content =
            contentRepository.findByIdOrNull(contentId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_CONTENT.message)
        return commentRepository
            .findByContentId(content.id!!)
            .map {
                it.toCommentReadResponse()
            }
    }

    // 댓글은 그룹 유저만 달 수 있게??
    @Transactional
    fun create(
        email: String,
        request: CommentCreateRequest,
    ): CommentCreateResponse {
        val userResponse = validate(email, request.groupId)
        if (request.type == CommentType.CONTENT) {
            val content =
                contentRepository.findByIdOrNull(request.contentId) ?: throw RestException.notFound(
                    ErrorMessage.NOT_FOUND_CONTENT.message,
                )

            return save(content.id!!, request, userResponse).toCommentCreateResponse()
        }

        val content =
            announcementRepository.findByIdOrNull(request.contentId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_ANNOUNCE.message,
            )
        return save(content.id!!, request, userResponse).toCommentCreateResponse()
    }

    // 해당 댓글 작성자인지 확인
    @Transactional
    fun update(
        email: String,
        request: CommentUpdateRequest,
    ): CommentUpdateResponse {
        val userResponse = validate(email, request.groupId)
        val comment =
            commentRepository.findByContentIdAndId(request.contentId, request.commentId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_COMMENT.message,
            )

        if (!comment.groupUser.user.email
                .equals(email)
        ) {
            throw RestException.badRequest(ErrorMessage.POSSIBLE_WRITER_UPDATE.message)
        }

        return comment
            .apply {
                this.content = request.content
                request.mentions?.let {
                    commentMentionRepository.deleteByComment(this)
                    val mentions =
                        it.map {
                            val groupUser =
                                groupUserRepository.findByIdOrNull(it.groupUserId) ?: throw RestException.notFound(
                                    ErrorMessage.NOT_FOUND_GROUP_USER.message,
                                )
                            commentMentionRepository.save(
                                CommentMention(
                                    comment = this,
                                    groupUser = groupUser,
                                ),
                            )
                        }

                    comment.mentions.clear()
                    comment.mentions.addAll(mentions)
                }
            }.toCommentUpdateResponse()
    }

    @Transactional
    fun delete(
        email: String,
        groupId: Long,
        contentId: Long,
        commentId: Long,
    ) {
        val userResponse = validate(email, groupId)
        val comment =
            commentRepository.findByContentIdAndId(contentId, commentId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_COMMENT.message,
            )

        if (!comment.groupUser.user.email
                .equals(userResponse.user.email) &&
            !userResponse.groupUser!!.role.isAdmin()
        ) {
            throw RestException.badRequest(ErrorMessage.IMPOSSIBLE_DELETE.message)
        }
        commentMentionRepository.deleteAll(comment.mentions)
        commentRepository.delete(comment)
    }

    private fun save(
        contentId: Long,
        request: CommentCreateRequest,
        userResponse: UserValidateResponse,
    ): Comment {
        val comment =
            commentRepository.save(
                Comment(
                    contentId = contentId,
                    group = userResponse.group,
                    groupUser = userResponse.groupUser!!,
                    content = request.content,
                    type = request.type,
                ),
            )

        request.mentions?.map { user ->
            val groupUser =
                groupUserRepository.findByIdOrNull(user.groupUserId) ?: throw RestException.notFound(
                    ErrorMessage.NOT_FOUND_GROUP_USER.message,
                )

            commentMentionRepository.save(
                CommentMention(
                    comment = comment,
                    groupUser = groupUser,
                ),
            )
        }

        return comment
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

    private fun validatePublic(
        email: String,
        groupId: Long,
    ): UserValidateResponse {
        val user =
            userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        val groupUser =
            groupUserRepository.findByUserAndGroup(user, group)

        if (group.isPublic) {
            return UserValidateResponse(
                user = user,
                group = group,
                groupUser = groupUser,
            )
        }

        if (!groupUser!!.role.isActive()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return UserValidateResponse(
            user = user,
            group = group,
            groupUser = groupUser,
        )
    }
}
