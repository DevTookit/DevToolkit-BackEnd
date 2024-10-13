package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.group.GroupLogRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.response.GroupLogDetailResponse.Companion.toGroupLogDetailResponse
import com.project.api.web.dto.response.GroupLogResponse
import com.project.api.web.dto.response.GroupLogResponse.Companion.toGroupLogResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.content.Content
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupLog
import com.project.core.domain.user.User
import com.project.core.internal.ContentType
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupLogService(
    private val groupLogRepository: GroupLogRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val groupUserRepository: GroupUserRepository,
) {
    fun readAll(
        email: String,
        pageable: Pageable,
        groupId: Long,
        type: ContentType?,
    ): GroupLogResponse {
        val userResponse = validate(email, groupId)
        val logs =
            type?.let {
                groupLogRepository
                    .findByGroupAndType(userResponse.group, it, pageable)
                    .map {
                        it.toGroupLogDetailResponse()
                    }
            } ?: run {
                groupLogRepository.findByGroup(userResponse.group, pageable).map {
                    it.toGroupLogDetailResponse()
                }
            }

        return userResponse.group.toGroupLogResponse(logs)
    }

    @Transactional
    fun create(
        group: Group,
        user: User,
        content: Content,
        sectionId: Long,
    ) {
        groupLogRepository.save(
            GroupLog(
                user = user,
                group = group,
                sectionId = sectionId,
                type = content.type,
                contentId = content.id!!,
                contentName = content.name,
            ),
        )
    }

    private fun validate(
        email: String,
        groupId: Long,
    ): UserValidateResponse {
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        val user =
            userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        val groupUser = groupUserRepository.findByUserAndGroup(user, group)

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
