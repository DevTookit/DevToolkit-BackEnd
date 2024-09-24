package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.fixture.GroupFixture
import com.project.api.fixture.GroupUserFixture
import com.project.api.fixture.NotificationFixture
import com.project.api.fixture.SectionFixture
import com.project.api.fixture.UserFixture
import com.project.api.repository.notification.NotificationRepository
import com.project.core.internal.NotificationType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest(
    @Autowired private val notificationFixture: NotificationFixture,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val notificationService: NotificationService,
    @Autowired private val notificationRepository: NotificationRepository,
    @Autowired private val sectionFixture: SectionFixture,
) {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        notificationFixture.tearDown()
        groupUserFixture.tearDown()
        sectionFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun create() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)

        val notificationUser = userFixture.create()
        val groupUser = groupUserFixture.create(group = group, user = notificationUser)
        notificationService.create(
            user = notificationUser,
            group = group,
            contentId = groupUser.id!!,
            type = NotificationType.INVITATION,
        )

        val response = notificationRepository.findAll()

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].user.id).isEqualTo(notificationUser.id)
        Assertions.assertThat(response[0].group.id).isEqualTo(group.id)
        Assertions.assertThat(response[0].type).isEqualTo(NotificationType.INVITATION)
    }

    @Test
    fun readAllWhenTypeIsNoticeAndContent() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val category = sectionFixture.create(group = group)
        val notificationUser = userFixture.create()
        val notification1 =
            notificationFixture.create(
                user = notificationUser,
                group = group,
                type = NotificationType.NOTICE,
            )

        notificationFixture.create(
            user = notificationUser,
            group = group,
            type = NotificationType.CONTENT,
            section = category,
        )

        val response =
            notificationService.readAll(
                email = notificationUser.email,
                isRead = null,
                pageable = Pageable.unpaged(),
            )

        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun readAllWhenTypeIsMentionAndInvitation() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val notificationUser = userFixture.create()
        val notification1 =
            notificationFixture.create(
                user = notificationUser,
                group = group,
                type = NotificationType.MENTION,
            )

        notificationFixture.create(
            user = notificationUser,
            group = group,
            type = NotificationType.INVITATION,
        )

        val response =
            notificationService.readAll(
                email = notificationUser.email,
                isRead = null,
                pageable = Pageable.unpaged(),
            )

        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun update() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val notificationUser = userFixture.create()
        val notification =
            notificationFixture.create(
                user = notificationUser,
                group = group,
            )

        notificationService.update(notification.id!!)

        val response = notificationRepository.findAll()

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].isRead).isTrue()
    }

    @Test
    fun updateNotFoundNotification() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val notificationUser = userFixture.create()

        Assertions
            .assertThatThrownBy {
                notificationService.update(1L)
            }.isInstanceOf(RestException::class.java)
    }
}
