package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.repository.group.GroupLogRepository
import com.project.api.supprot.fixture.ContentFixture
import com.project.api.supprot.fixture.GroupFixture
import com.project.api.supprot.fixture.GroupUserFixture
import com.project.api.supprot.fixture.SectionFixture
import com.project.api.supprot.fixture.UserFixture
import com.project.api.web.dto.request.ContentCreateRequest
import com.project.api.web.dto.request.ContentUpdateRequest
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.user.User
import com.project.core.internal.ContentType
import com.project.core.internal.SectionType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

class ContentServiceTest(
    @Autowired private val contentService: ContentService,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val sectionFixture: SectionFixture,
    @Autowired private val contentFixture: ContentFixture,
    @Autowired private val groupLogRepository: GroupLogRepository,
) : TestCommonSetting() {
    lateinit var user: User
    lateinit var group: Group
    lateinit var groupUser: GroupUser

    @BeforeEach
    fun setup() {
        user = userFixture.create()
        group = groupFixture.create(user = user, isPublic = true)
        groupUser = groupUserFixture.create(group = group, user = userFixture.create())
    }

    @AfterEach
    fun tearDown() {
        contentFixture.tearDown()
        sectionFixture.tearDown()
        groupUserFixture.tearDown()
        groupLogRepository.deleteAll()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun readAll() {
        val group2 = groupFixture.create(user = user, isPublic = true)
        val groupUser2 = groupUserFixture.create(group = group2, user = userFixture.create())
        val section = sectionFixture.create(group = group)
        val section2 = sectionFixture.create(group = group2)
        val content =
            contentFixture.create(section = section, group = group, groupUser = groupUser, type = ContentType.CODE)
        contentFixture.create(section = section, group = group, groupUser = groupUser, type = ContentType.BOARD)
        contentFixture.create(section = section2, group = group2, groupUser = groupUser2, type = ContentType.CODE)

        val response =
            contentService.readAll(
                email = user.email,
                groupId = group.id,
                sectionId = section.id,
                name = null,
                languages = null,
                skills = null,
                writer = null,
                startDate = content.createdDate!! - 1000L,
                endDate = content.createdDate!! + 1000L,
                pageable = PageRequest.of(0, 10),
                type = null,
            )

        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun readAllGroupIdIsNull() {
        val section = sectionFixture.create(group = group)
        val content =
            contentFixture.create(section = section, group = group, groupUser = groupUser, type = ContentType.CODE)
        contentFixture.create(section = section, group = group, groupUser = groupUser, type = ContentType.BOARD)

        val response =
            contentService.readAll(
                email = user.email,
                groupId = null,
                sectionId = null,
                name = null,
                languages = null,
                skills = null,
                writer = null,
                startDate = content.createdDate!! - 1000L,
                endDate = content.createdDate!! + 1000L,
                pageable = PageRequest.of(0, 10),
                type = null,
            )

        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun read() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.CODE, group = group)

        val response =
            contentService.read(
                email = user.email,
                groupId = group.id!!,
                sectionId = section.id!!,
                contentId = content.id!!,
            )

        Assertions.assertThat(response.content).isEqualTo(content.content)
        Assertions.assertThat(response.contentId).isEqualTo(content.id)
        Assertions.assertThat(response.name).isEqualTo(content.name)
        Assertions.assertThat(response.type).isEqualTo(content.type)
    }

    @Test
    fun readNotFoundSection() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.CODE, group = group)

        Assertions
            .assertThatThrownBy {
                contentService.read(user.email, group.id!!, 100L, content.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readNotFoundContent() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)

        Assertions
            .assertThatThrownBy {
                contentService.read(user.email, group.id!!, section.id!!, 100L)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun create() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val request =
            ContentCreateRequest(
                name = "name",
                languages = null,
                skills = null,
                content = "content",
                codeDescription = null,
                type = ContentType.BOARD,
            )

        val response =
            contentService.create(
                email = user.email,
                groupId = group.id!!,
                sectionId = section.id!!,
                request = request,
                files = null,
            )

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.type).isEqualTo(request.type)
    }

    @Test
    fun createNotFoundSection() {
        val request =
            ContentCreateRequest(
                name = "name",
                languages = null,
                skills = null,
                content = "content",
                codeDescription = null,
                type = ContentType.BOARD,
            )

        Assertions.assertThatThrownBy {
            contentService.create(
                email = user.email,
                groupId = group.id!!,
                sectionId = 100L,
                request = request,
                files = null,
            )
        }
    }

    @Test
    fun update() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(section = section, group = group, groupUser = groupUser, type = ContentType.CODE)

        val request =
            ContentUpdateRequest(
                contentId = content.id!!,
                name = "content",
                languages = listOf("Java", "Kotlin"),
                skills = null,
                content = "cotent",
                codeDescription = "codeDscription",
                type = ContentType.CODE,
            )

        val response = contentService.update(groupUser.user.email, group.id!!, section.id!!, request, null)

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.contentId).isEqualTo(request.contentId)
        Assertions.assertThat(response.languages?.size).isEqualTo(request.languages!!.size)
    }

    @Test
    fun updateNotFoundSection() {
    }

    @Test
    fun updateNotFoundContent() {
    }

    @Test
    fun delete() {
    }

    @Test
    fun deleteNotFoundSection() {
    }

    @Test
    fun readHot() {
    }
}
