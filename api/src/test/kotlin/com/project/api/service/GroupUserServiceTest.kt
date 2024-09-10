package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.fixture.GroupFixture
import com.project.api.fixture.GroupUserFixture
import com.project.api.fixture.UserFixture
import com.project.api.repository.group.GroupUserRepository
import com.project.core.internal.GroupRole
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class GroupUserServiceTest(
    @Autowired private val groupUserService: GroupUserService,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val groupUserRepository: GroupUserRepository,
) {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        groupUserFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun deleteByTopManager() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager = groupUserFixture.create(
            group = group,
            user = manager,
            role = GroupRole.MANAGER
        )

        val groupUser = groupUserFixture.create(
            group = group,
            user = user,
            role = GroupRole.USER
        )

        groupUserService.delete(admin.email, group.id!!, groupManager.id!!)

        val response = groupUserRepository.findAll()
        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun deleteByManager() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager = groupUserFixture.create(
            group = group,
            user = manager,
            role = GroupRole.MANAGER
        )

        val groupUser = groupUserFixture.create(
            group = group,
            user = user,
            role = GroupRole.USER
        )

        groupUserService.delete(manager.email, group.id!!, groupUser.id!!)

        val response = groupUserRepository.findAll()
        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun deleteManagerNotFound() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)

        val groupUser = groupUserFixture.create(
            group = group,
            user = user,
            role = GroupRole.USER
        )

        Assertions.assertThatThrownBy {
            groupUserService.delete(manager.email, group.id!!, groupUser.id!!)
        }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteUserNotFound() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)

        val groupManager = groupUserFixture.create(
            group = group,
            user = manager,
            role = GroupRole.MANAGER
        )

        Assertions.assertThatThrownBy {
            groupUserService.delete(manager.email, group.id!!, 100L)
        }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteIfNotSameRole() {
        val admin = userFixture.create()
        val manager1 = userFixture.create()
        val manager2 = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager1 = groupUserFixture.create(
            group = group,
            user = manager1,
            role = GroupRole.MANAGER
        )

        val groupManager2 = groupUserFixture.create(
            group = group,
            user = manager2,
            role = GroupRole.MANAGER
        )

        Assertions.assertThatThrownBy {
            groupUserService.delete(manager1.email, group.id!!, groupManager2.id!!)
        }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteNotAllowedIfRoleIsUser() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager = groupUserFixture.create(
            group = group,
            user = manager,
            role = GroupRole.MANAGER
        )

        val groupUser = groupUserFixture.create(
            group = group,
            user = user,
            role = GroupRole.USER
        )

        Assertions.assertThatThrownBy {
            groupUserService.delete(user.email, group.id!!, groupManager.id!!)
        }.isInstanceOf(RestException::class.java)
    }

}