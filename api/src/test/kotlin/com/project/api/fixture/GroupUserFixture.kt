package com.project.api.fixture

import com.project.api.repository.group.GroupUserRepository
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.user.User
import com.project.core.internal.GroupRole
import org.springframework.stereotype.Component

@Component
class GroupUserFixture(
    private val groupUserRepository: GroupUserRepository,
) : Fixture {
    fun create(
        group: Group,
        user: User,
        role: GroupRole = GroupRole.USER,
    ): GroupUser =
        groupUserRepository.save(
            GroupUser(
                group = group,
                user = user,
                role = role,
            ).apply {
                isAccepted = true
            },
        )

    override fun tearDown() {
        groupUserRepository.deleteAll()
    }
}
