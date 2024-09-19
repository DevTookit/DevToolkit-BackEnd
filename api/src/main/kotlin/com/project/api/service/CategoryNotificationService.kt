package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.category.CategoryNotificationRepository
import com.project.api.repository.category.CategoryRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.CategoryNotificationUpdateRequest
import com.project.api.web.dto.response.CategoryNotificationUpdateResponse
import com.project.api.web.dto.response.CategoryNotificationUpdateResponse.Companion.toCategoryNotificationUpdateResponse
import com.project.core.domain.category.Category
import com.project.core.domain.category.CategoryNotification
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.internal.CategoryNotificationType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryNotificationService(
    private val categoryNotificationRepository: CategoryNotificationRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val groupUserRepository: GroupUserRepository,
    private val groupRepository: GroupRepository,
) {
    @Transactional
    fun create(
        category: Category,
        groupUser: GroupUser,
    ) {
        categoryNotificationRepository.save(
            CategoryNotification(
                category = category,
                groupUser = groupUser,
            ),
        )
    }

    @Transactional
    fun update(
        email: String,
        request: CategoryNotificationUpdateRequest,
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

        val category =
            categoryRepository.findByIdOrNull(request.categoryId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_CATEGORY.message,
            )

        val categoryNotification =
            categoryNotificationRepository.findByCategoryAndGroupUser(category, groupUser)?.apply {
                this.type = request.type
            } ?: run {
                categoryNotificationRepository.save(
                    CategoryNotification(
                        category = category,
                        groupUser = groupUser,
                        type = request.type,
                    ),
                )
            }

        return categoryNotification.toCategoryNotificationUpdateResponse(category.id)
    }

    @Transactional
    fun update(
        group: Group,
        groupUser: GroupUser,
        type: CategoryNotificationType,
    ) {
        categoryRepository
            .findByGroup(group)
            .map { category ->
                categoryNotificationRepository
                    .findByCategoryAndGroupUser(
                        category,
                        groupUser,
                    )?.apply {
                        this.type = type
                    } ?: run {
                    categoryNotificationRepository.save(
                        CategoryNotification(
                            category = category,
                            groupUser = groupUser,
                            type = type,
                        ),
                    )
                }
            }
    }
}
