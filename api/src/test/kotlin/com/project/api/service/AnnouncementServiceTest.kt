package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.repository.announcement.AnnouncementRepository
import com.project.api.supprot.fixture.GroupFixture
import com.project.api.supprot.fixture.GroupUserFixture
import com.project.api.supprot.fixture.UserFixture
import com.project.api.web.dto.request.AnnounceCreateRequest
import com.project.api.web.dto.request.AnnounceUpdateRequest
import com.project.core.domain.announcement.Announcement
import com.project.core.internal.GroupRole
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable

class AnnouncementServiceTest(
    @Autowired private val announcementService: AnnouncementService,
    @Autowired private val announcementRepository: AnnouncementRepository,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
) : TestCommonSetting() {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        announcementRepository.deleteAll()
        groupUserFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun readAll() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)
        val groupUser = groupUserFixture.create(group = group, user = userFixture.create(), role = GroupRole.TOP_MANAGER)
        val announcement =
            announcementRepository.save(
                Announcement(
                    group = group,
                    groupUser = groupUser,
                    name = "공지사항",
                    content = "공지사항임다",
                ),
            )

        val response =
            announcementService.readAll(
                email = user.email,
                groupId = group.id!!,
                pageable = Pageable.unpaged(),
            )

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].announceId).isEqualTo(announcement.id)
    }

    @Test
    fun read() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)
        val groupUser = groupUserFixture.create(group = group, user = userFixture.create(), role = GroupRole.TOP_MANAGER)
        val announcement =
            announcementRepository.save(
                Announcement(
                    group = group,
                    groupUser = groupUser,
                    name = "공지사항",
                    content = "공지사항임다",
                ),
            )

        val response =
            announcementService.read(
                email = user.email,
                groupId = group.id!!,
                announceId = announcement.id!!,
            )

        Assertions.assertThat(response.announceId).isEqualTo(announcement.id)
        Assertions.assertThat(response.name).isEqualTo(announcement.name)
        Assertions.assertThat(response.content).isEqualTo(announcement.content)
    }

    @Test
    fun readNotFoundAnnounce() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)
        val groupUser = groupUserFixture.create(group = group, user = userFixture.create(), role = GroupRole.TOP_MANAGER)

        Assertions
            .assertThatThrownBy {
                announcementService.read(
                    email = user.email,
                    groupId = group.id!!,
                    announceId = 0L,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun create() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)
        val request =
            AnnounceCreateRequest(
                name = "name",
                content = "content",
            )

        val response =
            announcementService.create(
                email = user.email,
                groupId = group.id!!,
                request = request,
            )

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.content).isEqualTo(request.content)
    }

    @Test
    fun createRoleIsNotAdmin() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)
        val user2 = userFixture.create()
        groupUserFixture.create(group = group, user = user2, role = GroupRole.USER)
        val request =
            AnnounceCreateRequest(
                name = "name",
                content = "content",
            )

        Assertions
            .assertThatThrownBy {
                announcementService.create(
                    email = user2.email,
                    groupId = group.id!!,
                    request = request,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun update() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)
        val groupUser = groupUserFixture.create(group = group, user = userFixture.create(), role = GroupRole.TOP_MANAGER)
        val announcement =
            announcementRepository.save(
                Announcement(
                    group = group,
                    groupUser = groupUser,
                    name = "공지사항",
                    content = "공지사항임다",
                ),
            )
        val request =
            AnnounceUpdateRequest(
                name = "name",
                content = "content",
                announceId = announcement.id!!,
            )

        val response =
            announcementService.update(
                email = user.email,
                groupId = group.id!!,
                request = request,
            )

        Assertions.assertThat(response.announceId).isEqualTo(announcement.id)
        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.content).isEqualTo(request.content)
    }

    @Test
    fun updateRoleIsNotAdmin() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)
        val user2 = userFixture.create()
        val groupUser = groupUserFixture.create(group = group, user = user2, role = GroupRole.USER)
        val announcement =
            announcementRepository.save(
                Announcement(
                    group = group,
                    groupUser = groupUser,
                    name = "공지사항",
                    content = "공지사항임다",
                ),
            )
        val request =
            AnnounceUpdateRequest(
                name = "name",
                content = "content",
                announceId = announcement.id!!,
            )

        Assertions
            .assertThatThrownBy {
                announcementService.update(
                    email = user2.email,
                    groupId = group.id!!,
                    request = request,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateNotFoundAnnounce() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)
        val groupUser = groupUserFixture.create(group = group, user = userFixture.create(), role = GroupRole.TOP_MANAGER)
        val request =
            AnnounceUpdateRequest(
                name = "name",
                content = "content",
                announceId = 0L,
            )

        Assertions
            .assertThatThrownBy {
                announcementService.update(
                    email = user.email,
                    groupId = group.id!!,
                    request = request,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun delete() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)
        val groupUser = groupUserFixture.create(group = group, user = userFixture.create(), role = GroupRole.TOP_MANAGER)
        val announcement =
            announcementRepository.save(
                Announcement(
                    group = group,
                    groupUser = groupUser,
                    name = "공지사항",
                    content = "공지사항임다",
                ),
            )

        announcementService.delete(
            email = user.email,
            groupId = group.id!!,
            announceId = announcement.id!!,
        )

        val response = announcementRepository.findAll()

        Assertions.assertThat(response).isEmpty()
    }

    @Test
    fun deleteRoleIsNotAdmin() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user)
        val user2 = userFixture.create()
        val groupUser = groupUserFixture.create(group = group, user = user2, role = GroupRole.USER)
        val announcement =
            announcementRepository.save(
                Announcement(
                    group = group,
                    groupUser = groupUser,
                    name = "공지사항",
                    content = "공지사항임다",
                ),
            )

        Assertions.assertThatThrownBy {
            announcementService.delete(
                email = user2.email,
                groupId = group.id!!,
                announceId = announcement.id!!,
            )
        }
    }
}
