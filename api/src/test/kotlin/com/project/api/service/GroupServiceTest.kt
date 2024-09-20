package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.fixture.GroupFixture
import com.project.api.fixture.GroupUserFixture
import com.project.api.fixture.UserFixture
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.web.dto.request.GroupCreateRequest
import com.project.api.web.dto.request.GroupUpdateRequest
import com.project.core.internal.GroupRole
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
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
        groupUserFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun readAll() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)

        val response = groupService.readAll(null, Pageable.unpaged())

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response.first().name).isEqualTo(group.name)
        Assertions.assertThat(response.first().description).isEqualTo(group.description)
        Assertions.assertThat(response.first().id).isEqualTo(group.id)
        Assertions.assertThat(response.first().isPublic).isEqualTo(group.isPublic)
    }

    @Test
    fun readOneGroupIsPublic() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = true)

        val response = groupService.readOne(user.email, group.id!!)

        Assertions.assertThat(response.img).isEqualTo(group.img)
        Assertions.assertThat(response.name).isEqualTo(group.name)
        Assertions.assertThat(response.isPublic).isEqualTo(group.isPublic)
        Assertions.assertThat(response.id).isEqualTo(group.id)
    }

    @Test
    fun readOneGroupIsPrivate() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = false)

        val response = groupService.readOne(user.email, group.id!!)

        Assertions.assertThat(response.img).isEqualTo(group.img)
        Assertions.assertThat(response.name).isEqualTo(group.name)
        Assertions.assertThat(response.isPublic).isEqualTo(group.isPublic)
        Assertions.assertThat(response.id).isEqualTo(group.id)
    }

    @Test
    fun readOneGroupUserIsNotActive() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = false)

        val user2 = userFixture.create()
        val notActiveGroupUser = groupUserFixture.create(group = group, user = user2, role = GroupRole.PENDING)

        Assertions
            .assertThatThrownBy {
                groupService.readOne(user2.email, group.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readOneNotFoundGroup() {
        val user = userFixture.create()

        Assertions
            .assertThatThrownBy {
                groupService.readOne(user.email, 1L)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun create() {
        val user = userFixture.create()
        val request =
            GroupCreateRequest(
                name = "Group",
                description = null,
                isPublic = true,
            )

        val response = groupService.create(user.email, request, null)
        val responseGroupUsers = groupUserRepository.findAll()

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
                description = null,
                isPublic = true,
            )

        Assertions
            .assertThatThrownBy {
                groupService.create("haha@gmail.com", request, null)
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
}
