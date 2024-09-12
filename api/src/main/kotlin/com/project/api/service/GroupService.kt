package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.GroupCreateRequest
import com.project.api.web.dto.request.GroupRoleUpdateRequest
import com.project.api.web.dto.request.GroupUpdateRequest
import com.project.api.web.dto.response.GroupResponse
import com.project.api.web.dto.response.GroupResponse.Companion.toResponse
import com.project.api.web.dto.response.GroupRoleUpdateResponse
import com.project.api.web.dto.response.GroupRoleUpdateResponse.Companion.toGroupRoleUpdateResponse
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.internal.GroupRole
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val userRepository: UserRepository,
) {
    fun readOne(
        email: String,
        groupId: Long,
    ): GroupResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        if (group.isPublic) {
            return group.toResponse()
        }

        val groupUser =
            groupUserRepository.findByUserAndGroup(user, group) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)
        if (groupUser.role.isActive()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return group.toResponse()
    }

    @Transactional
    fun create(
        email: String,
        request: GroupCreateRequest,
    ): GroupResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository
                .save(
                    Group(
                        user = user,
                        name = request.name,
                        description = request.description,
                        img = request.img,
                        isPublic = request.isPublic,
                    ),
                ).also {
                    groupUserRepository.save(
                        GroupUser(
                            user = user,
                            group = it,
                            role = GroupRole.TOP_MANAGER,
                        ).apply {
                            isAccepted = true
                        },
                    )
                }

        return group.toResponse()
    }

    @Transactional
    fun update(
        email: String,
        request: GroupUpdateRequest,
    ): GroupResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(request.id)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        if (group.user.id != user.id) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        return groupRepository
            .save(
                group.apply {
                    this.name = request.name
                    this.img = request.img
                    this.description = request.description
                    this.isPublic = request.isPublic
                },
            ).toResponse()
    }

    @Transactional
    fun delete(
        email: String,
        id: Long,
    ) {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(id)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        if (group.user.id != user.id) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        groupRepository.delete(group)
    }

    @Transactional
    fun updateRole(
        email: String,
        request: GroupRoleUpdateRequest,
    ): GroupRoleUpdateResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(request.groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        if (group.user.id != user.id) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        val groupUser =
            groupUserRepository.findByIdOrNull(request.groupUserId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_GROUP_USER.message,
            )

        return groupUserRepository
            .save(
                groupUser.apply {
                    this.role = request.role
                },
            ).toGroupRoleUpdateResponse(group.id!!)
    }
}
