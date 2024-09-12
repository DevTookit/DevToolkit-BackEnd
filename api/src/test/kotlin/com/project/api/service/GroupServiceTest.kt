package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.fixture.GroupFixture
import com.project.api.fixture.GroupUserFixture
import com.project.api.fixture.UserFixture
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.web.dto.request.GroupCreateRequest
import com.project.api.web.dto.request.GroupRoleUpdateRequest
import com.project.api.web.dto.request.GroupUpdateRequest
import com.project.core.internal.GroupRole
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class GroupServiceTest(
    @Autowired private val groupService: GroupService,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val groupUserRepository: GroupUserRepository,
    @Autowired private val groupRepository: GroupRepository,
) {
    @AfterEach
    fun tearDown() {
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun create() {
        val user = userFixture.create()
        val request =
            GroupCreateRequest(
                name = "Group",
                img = null,
                description = null,
                isPublic = true,
            )

        val response = groupService.create(user.email, request)
        val responseGroupUsers = groupUserRepository.findAll()

        Assertions.assertThat(response.img).isEqualTo(request.img)
        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.description).isEqualTo(request.description)
        Assertions.assertThat(response.isPublic).isEqualTo(request.isPublic)
        Assertions.assertThat(responseGroupUsers[0].role).isEqualTo(GroupRole.TOP_MANAGER)
        Assertions.assertThat(responseGroupUsers[0].isAccepted).isEqualTo(true)
        Assertions.assertThat(responseGroupUsers[0].user.id).isEqualTo(user.id)
    }

    @Test
    fun createNotFoundUser() {
        val request =
            GroupCreateRequest(
                name = "Group",
                img = null,
                description = null,
                isPublic = true,
            )

        Assertions
            .assertThatThrownBy {
                groupService.create("haha@gmail.com", request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun update() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)

        val request =
            GroupUpdateRequest(
                id = group.id!!,
                name = "Group",
                img = "img",
                description = "description",
                isPublic = true,
            )

        val response = groupService.update(user.email, request)

        Assertions.assertThat(response.img).isEqualTo(request.img)
        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.description).isEqualTo(request.description)
        Assertions.assertThat(response.isPublic).isEqualTo(request.isPublic)
    }

    @Test
    fun updateNotFoundGroup() {
        val user = userFixture.create()

        val request =
            GroupUpdateRequest(
                id = 1L,
                name = "Group",
                img = "img",
                description = "description",
                isPublic = true,
            )

        Assertions
            .assertThatThrownBy {
                groupService.update(user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateOnlyByHost() {
        val user = userFixture.create()
        val notHost = userFixture.create()
        val group = groupFixture.create(user = user)

        val request =
            GroupUpdateRequest(
                id = group.id!!,
                name = "Group",
                img = "img",
                description = "description",
                isPublic = true,
            )

        Assertions
            .assertThatThrownBy {
                groupService.update(notHost.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun delete() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)

        groupService.delete(user.email, group.id!!)

        val responseGroup = groupRepository.findAll()
        val responseGroupUsers = groupUserRepository.findAll()

        Assertions.assertThat(responseGroup).isEmpty()
        Assertions.assertThat(responseGroupUsers).isEmpty()
    }

    @Test
    fun deleteNotFoundGroup() {
        val user = userFixture.create()

        Assertions
            .assertThatThrownBy {
                groupService.delete(user.email, 1L)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteOnlyByHost() {
        val user = userFixture.create()
        val notHost = userFixture.create()
        val group = groupFixture.create(user = user)

        Assertions
            .assertThatThrownBy {
                groupService.delete(notHost.email, group.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateRole() {
        val admin = userFixture.create()
        val group = groupFixture.create(user = admin)

        val user = userFixture.create()
        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.USER,
            )

        val request =
            GroupRoleUpdateRequest(
                groupId = group.id!!,
                groupUserId = groupUser.id!!,
                role = GroupRole.MANAGER,
            )
        val response = groupService.updateRole(admin.email, request)

        Assertions.assertThat(response.role).isEqualTo(request.role)
        Assertions.assertThat(response.groupId).isEqualTo(request.groupId)
        Assertions.assertThat(response.groupUserId).isEqualTo(request.groupUserId)
    }

    @Test
    fun updateRoleForTopManagerOnly() {
        val admin = userFixture.create()
        val group = groupFixture.create(user = admin)

        val manager = userFixture.create()
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )

        val user = userFixture.create()
        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.USER,
            )

        val request =
            GroupRoleUpdateRequest(
                groupId = group.id!!,
                groupUserId = groupUser.id!!,
                role = GroupRole.MANAGER,
            )

        Assertions
            .assertThatThrownBy {
                groupService.updateRole(manager.email, request)
            }.isInstanceOf(RestException::class.java)
    }
}
