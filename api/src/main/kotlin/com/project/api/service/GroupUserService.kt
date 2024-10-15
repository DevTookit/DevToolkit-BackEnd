package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.internal.RedisType
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.GroupUserCreateRequest
import com.project.api.web.dto.request.GroupUserInvitationRequest
import com.project.api.web.dto.request.GroupUserUpdateRequest
import com.project.api.web.dto.response.GroupInvitationResponse
import com.project.api.web.dto.response.GroupInvitationResponse.Companion.toGroupInvitationResponse
import com.project.api.web.dto.response.GroupRoleResponse
import com.project.api.web.dto.response.GroupRoleResponse.Companion.toGroupRoleResponse
import com.project.api.web.dto.response.GroupUserCreateResponse
import com.project.api.web.dto.response.GroupUserCreateResponse.Companion.toGroupUserCreateResponse
import com.project.api.web.dto.response.GroupUserResponse
import com.project.api.web.dto.response.GroupUserResponse.Companion.toGroupUserResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.group.GroupUser
import com.project.core.domain.group.QGroupUser
import com.project.core.internal.GroupRole
import com.project.core.internal.NotificationType
import com.project.core.internal.SectionNotificationType
import com.querydsl.core.BooleanBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupUserService(
    private val groupUserRepository: GroupUserRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val sectionNotificationService: SectionNotificationService,
    private val notificationService: NotificationService,
    private val redisService: RedisService,
) {
    @Transactional
    fun create(
        email: String,
        request: GroupUserCreateRequest,
    ): GroupUserCreateResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(request.groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        groupUserRepository.existsByUserAndGroup(user, group).let {
            if (it) {
                throw RestException.conflict(ErrorMessage.CONFLICT_ENTITY.message)
            } else {
                redisService.addList(RedisType.JOIN_GROUP.name, group.id!!)

                return groupUserRepository
                    .save(
                        GroupUser(
                            user = user,
                            group = group,
                            role = request.role,
                        ).apply {
                            request.name?.let {
                                this.name = it
                            }
                        },
                    ).toGroupUserCreateResponse(group.id)
            }
        }
    }

    @Transactional
    fun createInvitation(
        email: String,
        request: GroupUserInvitationRequest,
    ): GroupInvitationResponse {
        val admin = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(request.groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        if (!group.user.id!!.equals(admin.id)) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        val user =
            userRepository.findByIdOrNull(request.userId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        return groupUserRepository
            .save(
                GroupUser(
                    user = user,
                    group = group,
                    role = GroupRole.INVITED,
                ),
            ).also {
                notificationService.create(
                    user = user,
                    group = group,
                    contentId = it.id!!,
                    type = NotificationType.INVITATION,
                )
            }.toGroupInvitationResponse()
    }

    fun readInvitations(email: String): List<GroupInvitationResponse> {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        return groupUserRepository
            .findByUserAndRole(user, GroupRole.INVITED)
            .map {
                it.toGroupInvitationResponse()
            }
    }

    @Transactional
    fun acceptInvitation(
        email: String,
        groupUserId: Long,
        isAccepted: Boolean,
    ) {
        userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val groupUser =
            groupUserRepository.findByIdOrNull(groupUserId) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)

        if (!isAccepted) {
            groupUserRepository.delete(groupUser)
            return
        }

        groupUser
            .apply {
                this.role = GroupRole.USER
                this.isApproved = true
                this.isAccepted = true
            }
    }

    @Transactional
    fun update(
        email: String,
        request: GroupUserUpdateRequest,
    ): GroupUserResponse {
        val userResponse = validate(email, request.groupId)

        // groupRole 거르고 만약 Manager가 Manger로 승격시키는 것은 불가능
        if (!userResponse.groupUser!!.role.isAdmin() ||
            userResponse.groupUser.role == request.role
        ) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        val groupUser = (
            groupUserRepository.findByIdOrNull(request.groupUserId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)
        )

        return groupUser
            .apply {
                if (request.role == GroupRole.SUSPENDED) {
                    this.role = request.role
                } else {
                    this.role = request.role
                    this.isApproved = true
                }
            }.also {
                val sectionNotificationType = if (!request.role.isActive()) SectionNotificationType.NONE else SectionNotificationType.ALL
                sectionNotificationService.update(userResponse.group, groupUser, sectionNotificationType)
            }.toGroupUserResponse()
    }

    @Transactional
    fun delete(
        email: String,
        groupId: Long,
        groupUserId: Long,
    ) {
        val userResponse = validate(email, groupId)
        if (!userResponse.groupUser!!.role.isAdmin()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        val expelUser =
            groupUserRepository.findByIdAndGroup(groupUserId, userResponse.group) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_GROUP_USER.message,
            )

        if (userResponse.groupUser.role == expelUser.role) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        } else {
            groupUserRepository.delete(expelUser)
        }
    }

    @Transactional
    fun deleteMe(
        email: String,
        groupId: Long,
    ) {
        val userResponse = validate(email, groupId)
        if (userResponse.group.user.id == userResponse.user.id) {
            throw RestException.conflict(ErrorMessage.GROUP_OWNER_CANNOT_LEAVE.message)
        }
        groupUserRepository.delete(userResponse.groupUser!!)
    }

    fun readRole(
        email: String,
        groupId: Long,
    ): GroupRoleResponse {
        val userResponse = validate(email, groupId)

        return userResponse.groupUser!!.toGroupRoleResponse(userResponse.group.id)
    }

    fun readAll(
        email: String,
        groupId: Long,
        role: GroupRole?,
        name: String?,
        isAccepted: Boolean?,
        isApproved: Boolean?,
        pageable: Pageable,
    ): Page<GroupUserResponse> {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)
        val groupUser =
            groupUserRepository.findByUserAndGroup(user, group) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)

        if (!groupUser.role.isActive()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return groupUserRepository
            .findAll(
                BooleanBuilder()
                    .and(
                        QGroupUser.groupUser.group.eq(group),
                    ).and(
                        role?.let { QGroupUser.groupUser.role.eq(it) },
                    ).and(
                        name?.let { QGroupUser.groupUser.name.containsIgnoreCase(it) },
                    ).and(
                        isAccepted?.let { QGroupUser.groupUser.isAccepted.eq(it) },
                    ).and(
                        isApproved?.let { QGroupUser.groupUser.isApproved.eq(it) },
                    ),
                pageable,
            ).map {
                it.toGroupUserResponse()
            }
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
