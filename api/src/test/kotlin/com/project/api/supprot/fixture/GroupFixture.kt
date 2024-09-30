package com.project.api.supprot.fixture

import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.user.User
import com.project.core.internal.GroupRole
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.random.Random

@Component
class GroupFixture(
    private val groupUserRepository: GroupUserRepository,
    private val groupRepository: GroupRepository,
) : Fixture {
    fun create(
        user: User,
        name: String = UUID.randomUUID().toString(),
        img: String? = null,
        description: String? = null,
        role: GroupRole = GroupRole.TOP_MANAGER,
        isPublic: Boolean = Random.nextBoolean(),
    ): Group =
        groupRepository
            .save(
                Group(
                    user = user,
                    name = name,
                    img = img,
                    description = description,
                    isPublic = isPublic,
                ),
            ).also {
                groupUserRepository.save(
                    GroupUser(
                        user = user,
                        group = it,
                        role = role,
                    ).apply {
                        this.isAccepted = true
                    },
                )
            }

    override fun tearDown() {
        groupUserRepository.deleteAll()
        groupRepository.deleteAll()
    }
}
