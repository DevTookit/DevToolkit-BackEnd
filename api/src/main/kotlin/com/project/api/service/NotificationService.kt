package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.notification.NotificationRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.response.NotificationResponse
import com.project.api.web.dto.response.NotificationResponse.Companion.toResponse
import com.project.core.domain.group.Group
import com.project.core.domain.notification.Notification
import com.project.core.domain.notification.QNotification
import com.project.core.domain.section.Section
import com.project.core.domain.user.User
import com.project.core.internal.NotificationType
import com.querydsl.core.BooleanBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun create(
        user: User,
        group: Group,
        contentId: Long,
        type: NotificationType,
    ) {
        notificationRepository.save(
            Notification(
                user = user,
                group = group,
                contentId = contentId,
                type = type,
            ),
        )
    }

    fun readAll(
        email: String,
        isRead: Boolean?,
        pageable: Pageable,
    ): Page<NotificationResponse> {
        val user =
            userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        return notificationRepository
            .findAll(
                BooleanBuilder()
                    .and(
                        isRead?.let { QNotification.notification.isRead.eq(it) },
                    ).and(
                        QNotification.notification.user.id
                            .eq(user.id),
                    ),
                pageable,
            ).map {
                it.toResponse(createNotificationContent(it.type, it.section, it.group))
            }
    }

    @Transactional
    fun update(notificationId: Long) {
        val notification =
            notificationRepository.findByIdOrNull(notificationId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_NOTIFICATION.message,
            )

        notification.apply {
            this.isRead = true
        }
    }

    private fun createNotificationContent(
        type: NotificationType,
        section: Section?,
        group: Group,
    ): String {
        when (type) {
            NotificationType.CONTENT -> return "${group.name}의 카테고리 ${section!!.name}에 새로운 컨텐츠가 게시되었습니다."
            NotificationType.NOTICE -> return "${group.name}에 공지사항을 확인해주세요"
            NotificationType.MENTION -> return "${group.name} 컨텐츠에 멘션되었습니다."
            else -> return "${group.name}에서 초대장을 보냈습니다."
        }
    }
}
