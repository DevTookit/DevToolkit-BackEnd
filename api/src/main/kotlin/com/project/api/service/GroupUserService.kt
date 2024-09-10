package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.core.internal.GroupRole
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupUserService(
    private val groupUserRepository: GroupUserRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun delete(
        email: String,
        groupId: Long,
        groupUserId: Long,
    ) {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        val admin =
            groupUserRepository.findByUserAndGroup(user, group) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)

        if (admin.role == GroupRole.USER) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        val expelUser =
            groupUserRepository.findByIdAndGroup(groupUserId, group) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_GROUP_USER.message,
            )

        if (admin.role == expelUser.role) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        } else groupUserRepository.delete(expelUser)

    }
}
