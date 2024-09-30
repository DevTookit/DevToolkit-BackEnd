package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.category.SectionNotificationRepository
import com.project.api.repository.category.SectionRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.SectionNotificationUpdateRequest
import com.project.api.web.dto.response.CategoryNotificationUpdateResponse
import com.project.api.web.dto.response.CategoryNotificationUpdateResponse.Companion.toCategoryNotificationUpdateResponse
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.section.Section
import com.project.core.domain.section.SectionNotification
import com.project.core.internal.SectionNotificationType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SectionNotificationService(
    private val sectionNotificationRepository: SectionNotificationRepository,
    private val sectionRepository: SectionRepository,
    private val userRepository: UserRepository,
    private val groupUserRepository: GroupUserRepository,
    private val groupRepository: GroupRepository,
) {
    @Transactional
    fun create(
        section: Section,
        groupUser: GroupUser,
    ) {
        sectionNotificationRepository.save(
            SectionNotification(
                section = section,
                groupUser = groupUser,
            ),
        )
    }

    @Transactional
    fun update(
        email: String,
        request: SectionNotificationUpdateRequest,
    ): CategoryNotificationUpdateResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(request.groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        val groupUser =
            groupUserRepository.findByUserAndGroup(user, group)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)

        if (!groupUser.role.isActive()) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        val section =
            sectionRepository.findByIdAndParentIsNull(request.sectionId) ?: throw RestException.notFound(
                ErrorMessage.IMPOSSIBLE_NOTIFICATION.message,
            )

        val sectionNotification =
            sectionNotificationRepository.findBySectionAndGroupUser(section, groupUser)?.apply {
                this.type = request.type
            } ?: run {
                sectionNotificationRepository.save(
                    SectionNotification(
                        section = section,
                        groupUser = groupUser,
                        type = request.type,
                    ),
                )
            }

        return sectionNotification.toCategoryNotificationUpdateResponse(section.id)
    }

    @Transactional
    fun update(
        group: Group,
        groupUser: GroupUser,
        type: SectionNotificationType,
    ) {
        sectionRepository
            .findByGroupAndParentIsNull(group)
            .map { section ->
                sectionNotificationRepository
                    .findBySectionAndGroupUser(
                        section,
                        groupUser,
                    )?.apply {
                        this.type = type
                    } ?: run {
                    sectionNotificationRepository.save(
                        SectionNotification(
                            section = section,
                            groupUser = groupUser,
                            type = type,
                        ),
                    )
                }
            }
    }
}
