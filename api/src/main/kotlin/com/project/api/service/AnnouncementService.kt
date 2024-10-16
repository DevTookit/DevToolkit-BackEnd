package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.announcement.AnnouncementRepository
import com.project.api.repository.comment.CommentRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.AnnounceCreateRequest
import com.project.api.web.dto.request.AnnounceUpdateRequest
import com.project.api.web.dto.response.AnnounceCreateResponse
import com.project.api.web.dto.response.AnnounceCreateResponse.Companion.toAnnounceCreateResponse
import com.project.api.web.dto.response.AnnounceUpdateResponse
import com.project.api.web.dto.response.AnnounceUpdateResponse.Companion.toAnnounceUpdateResponse
import com.project.api.web.dto.response.AnnouncementResponse
import com.project.api.web.dto.response.AnnouncementResponse.Companion.toResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.announcement.Announcement
import com.project.core.internal.CommentType
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AnnouncementService(
    private val announcementRepository: AnnouncementRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val commentRepository: CommentRepository,
) {
    @Transactional(readOnly = true)
    fun readAll(
        email: String,
        groupId: Long,
        pageable: Pageable,
    ): List<AnnouncementResponse> {
        val userResponse = validatePublic(email, groupId)

        return announcementRepository
            .findByGroup(userResponse.group, pageable)
            .map {
                val commentCnt = commentRepository.countByContentIdAndType(it.id!!, CommentType.ANNOUNCE)
                it.toResponse(commentCnt)
            }
    }

    @Transactional(readOnly = true)
    fun read(
        email: String,
        groupId: Long,
        announceId: Long,
    ): AnnouncementResponse {
        val userResponse = validatePublic(email, groupId)
        val announcement =
            announcementRepository.findByIdAndGroup(announceId, userResponse.group) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_ANNOUNCE.message,
            )
        val commentCnt = commentRepository.countByContentIdAndType(announcement.id!!, CommentType.ANNOUNCE)

        return announcement.toResponse(commentCnt)
    }

    @Transactional
    fun create(
        email: String,
        groupId: Long,
        request: AnnounceCreateRequest,
    ): AnnounceCreateResponse {
        val userResponse = validatePublic(email, groupId)
        if (!userResponse.groupUser!!.role.isAdmin()) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        return announcementRepository
            .save(
                Announcement(
                    group = userResponse.group,
                    groupUser = userResponse.groupUser,
                    name = request.name,
                    content = request.content,
                ),
            ).toAnnounceCreateResponse()
    }

    @Transactional
    fun update(
        email: String,
        groupId: Long,
        request: AnnounceUpdateRequest,
    ): AnnounceUpdateResponse {
        val userResponse = validatePublic(email, groupId)
        if (!userResponse.groupUser!!.role.isAdmin()) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        val announcement =
            announcementRepository.findByIdAndGroup(request.announceId, userResponse.group) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_ANNOUNCE.message,
            )

        return announcement
            .apply {
                request.name?.let { name -> this.name = name }
                request.content?.let { content -> this.content = content }
            }.toAnnounceUpdateResponse()
    }

    @Transactional
    fun delete(
        email: String,
        groupId: Long,
        announceId: Long,
    ) {
        val userResponse = validatePublic(email, groupId)
        if (!userResponse.groupUser!!.role.isAdmin()) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        announcementRepository.deleteByIdAndGroup(announceId, userResponse.group)
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

        if (groupUser != null && !groupUser.role.isActive()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return UserValidateResponse(
            user = user,
            group = group,
            groupUser = groupUser,
        )
    }
}
