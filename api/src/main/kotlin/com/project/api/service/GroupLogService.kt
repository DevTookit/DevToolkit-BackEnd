package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.group.GroupLogRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.response.GroupLogResponse
import com.project.api.web.dto.response.GroupLogResponse.Companion.toGroupLogResponse
import com.project.api.web.dto.response.UserValidateResponse
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
    @Transactional
    fun create() {
    }

    fun readAll(
        email: String,
        pageable: Pageable,
        groupId: Long,
        type: ContentType?,
    ): List<GroupLogResponse> {
        val userResponse = validate(email, groupId)

        return type?.let {
            groupLogRepository
                .findByGroupAndType(userResponse.group, it, pageable)
                .map {
                    it.toGroupLogResponse()
                }
        } ?: run {
            groupLogRepository.findByGroup(userResponse.group, pageable).map {
                it.toGroupLogResponse()
            }
        }
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
