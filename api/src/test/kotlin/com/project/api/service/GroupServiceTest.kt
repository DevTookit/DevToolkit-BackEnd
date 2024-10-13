package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.supprot.fixture.ContentFixture
import com.project.api.supprot.fixture.GroupFileAccessLogFixture
import com.project.api.supprot.fixture.GroupFixture
import com.project.api.supprot.fixture.GroupUserFixture
import com.project.api.supprot.fixture.SectionFixture
import com.project.api.supprot.fixture.UserFixture
import com.project.api.web.dto.request.GroupCreateRequest
import com.project.api.web.dto.request.GroupUpdateRequest
import com.project.core.internal.ContentType
import com.project.core.internal.GroupRole
import com.project.core.internal.SectionType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable

class GroupServiceTest(
    @Autowired private val groupService: GroupService,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val groupUserRepository: GroupUserRepository,
    @Autowired private val groupRepository: GroupRepository,
    @Autowired private val groupFileAccessLogFixture: GroupFileAccessLogFixture,
    @Autowired private val contentFixture: ContentFixture,
    @Autowired private val sectionFixture: SectionFixture,
) : TestCommonSetting() {
    @AfterEach
    fun tearDown() {
        groupFileAccessLogFixture.tearDown()
        contentFixture.tearDown()
        sectionFixture.tearDown()
        groupUserFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun readMine() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        groupFixture.create(userFixture.create())

        val response = groupService.readMine(user.email, Pageable.unpaged())

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].name).isEqualTo(group.name)
        Assertions.assertThat(response[0].img).isEqualTo(group.img)
        Assertions.assertThat(response[0].description).isEqualTo(group.description)
    }

    @Test
    fun readMe() {
        val user = userFixture.create()
        val group = groupFixture.create(userFixture.create())
        groupUserFixture.create(user = user, group = group)

        val response = groupService.readMe(user.email, Pageable.unpaged())

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].name).isEqualTo(group.name)
        Assertions.assertThat(response[0].img).isEqualTo(group.img)
        Assertions.assertThat(response[0].description).isEqualTo(group.description)
    }

    @Test
    fun readAll() {
        val admin = userFixture.create()
        val group = groupFixture.create(user = admin, isPublic = true)
        groupFixture.create(user = admin, isPublic = false)

        val response = groupService.readAll(null, Pageable.unpaged())

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response.size).isEqualTo(1)
        Assertions.assertThat(response.first().name).isEqualTo(group.name)
        Assertions.assertThat(response.first().description).isEqualTo(group.description)
        Assertions.assertThat(response.first().id).isEqualTo(group.id)
        Assertions.assertThat(response.first().isPublic).isEqualTo(group.isPublic)
    }

    @Test
    fun readOneGroupIsPublic() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = true)
        val visitor = userFixture.create()

        val response = groupService.readOne(visitor.email, group.id!!)

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
    fun readOneGroupIsPrivateAndUserNotGroupUser() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = false)
        val visitor = userFixture.create()

        Assertions
            .assertThatThrownBy {
                groupService.readOne(visitor.email, group.id!!)
            }.isInstanceOf(RestException::class.java)
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
                description = "description",
                isPublic = true,
            )

        val response = groupService.update(user.email, request, null)

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
                description = "description",
                isPublic = true,
            )

        Assertions
            .assertThatThrownBy {
                groupService.update(user.email, request, null)
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
                description = "description",
                isPublic = true,
            )

        Assertions
            .assertThatThrownBy {
                groupService.update(notHost.email, request, null)
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
    fun readRecentFilesGroupIsPublic() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = true)
        val user2 = userFixture.create()
        val groupUser = groupUserFixture.create(group = group, user = user2)
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(
                groupUser = groupUser,
                group = group,
                type = ContentType.FILE,
                section = section,
            )

        val visitor = userFixture.create()
        groupFileAccessLogFixture.create(
            user = visitor,
            content = content,
            group = group,
        )

        val response =
            groupService.readRecentFiles(groupId = group.id!!, email = visitor.email, Pageable.unpaged())

        Assertions.assertThat(response.creatorId).isEqualTo(user.id)
        Assertions.assertThat(response.creatorName).isEqualTo(user.name)
        Assertions.assertThat(response.creatorImg).isEqualTo(user.img)
        Assertions.assertThat(response.logs).isNotEmpty
    }

    @Test
    fun readRecentFilesGroupIsNotPublic() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = false)
        val user2 = userFixture.create()
        val groupUser = groupUserFixture.create(group = group, user = user2)
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(
                groupUser = groupUser,
                group = group,
                type = ContentType.FILE,
                section = section,
            )

        val visitor = userFixture.create()
        groupFileAccessLogFixture.create(
            user = visitor,
            content = content,
            group = group,
        )

        Assertions
            .assertThatThrownBy {
                groupService.readRecentFiles(groupId = group.id!!, email = visitor.email, Pageable.unpaged())
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readHot() {
        val admin = userFixture.create()
        val group1 = groupFixture.create(user = admin, isPublic = true)
        val group2 = groupFixture.create(user = admin, isPublic = true)

        val user1 = userFixture.create()
        val user2 = userFixture.create()
        groupUserFixture.create(group = group1, user = user1)
        groupUserFixture.create(group = group1, user = user2)
        groupUserFixture.create(group = group2, user = user2)

        val response = groupService.readHot()

        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun readHotCache() {
        val admin = userFixture.create()
        val group1 = groupFixture.create(user = admin, isPublic = true)
        val group2 = groupFixture.create(user = admin, isPublic = true)

        val user1 = userFixture.create()
        val user2 = userFixture.create()
        groupUserFixture.create(group = group1, user = user1)
        groupUserFixture.create(group = group1, user = user2)
        groupUserFixture.create(group = group2, user = user2)

        groupService.readHot()
        val response = groupService.readHot()

        Assertions.assertThat(response.size).isEqualTo(2)
    }
}
