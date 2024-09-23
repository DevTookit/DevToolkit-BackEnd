package com.project.api.fixture

import com.project.api.repository.notification.NotificationRepository
import com.project.core.domain.group.Group
import com.project.core.domain.notification.Notification
import com.project.core.domain.section.Section
import com.project.core.domain.user.User
import com.project.core.internal.NotificationType
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class NotificationFixture(
    private val notificationRepository: NotificationRepository,
) : Fixture {
    fun create(
        user: User,
        type: NotificationType = NotificationType.entries.random(),
        group: Group,
        contentId: Long = Random.nextLong(),
        section: Section? = null,
    ): Notification =
        notificationRepository.save(
            Notification(
                user = user,
                type = type,
                group = group,
                contentId = contentId,
            ).apply {
                this.section = section
            },
        )

    override fun tearDown() {
        notificationRepository.deleteAll()
    }
}
