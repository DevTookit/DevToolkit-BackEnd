package com.project.api.web.dto.response

import com.project.core.domain.notification.Notification
import com.project.core.internal.NotificationType

data class NotificationResponse(
    val type: NotificationType,
    val notificationId: Long?,
    val groupId: Long?,
    val contentId: Long,
    val content: String,
    val isRead: Boolean,
) {
    companion object {
        fun Notification.toResponse(content: String): NotificationResponse =
            NotificationResponse(
                type = this.type,
                notificationId = this.id,
                groupId = this.group.id,
                contentId = this.contentId,
                content = content,
                isRead = this.isRead,
            )
    }
}
