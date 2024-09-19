package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.category.CategoryRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.CategoryCreateRequest
import com.project.api.web.dto.request.CategoryUpdateRequest
import com.project.api.web.dto.response.CategoryCreateResponse
import com.project.api.web.dto.response.CategoryCreateResponse.Companion.toCategoryCreateResponse
import com.project.api.web.dto.response.CategoryUpdateResponse
import com.project.api.web.dto.response.CategoryUpdateResponse.Companion.toCategoryUpdateResponse
import com.project.core.domain.category.Category
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val groupUserRepository: GroupUserRepository,
    private val categoryNotificationService: CategoryNotificationService,
    private val groupRepository: GroupRepository,
) {
    @Transactional
    fun create(
        email: String,
        request: CategoryCreateRequest,
    ): CategoryCreateResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(request.groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        val groupUser =
            groupUserRepository.findByUserAndGroup(user, group)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)

        if (!groupUser.role.isTopAmin()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return categoryRepository
            .save(
                Category(
                    name = request.name,
                    group = group,
                    isPublic = request.isPublic,
                ),
            ).also {
                categoryNotificationService.create(it, groupUser)
            }.toCategoryCreateResponse(group.id)
    }

    @Transactional
    fun update(
        email: String,
        request: CategoryUpdateRequest,
    ): CategoryUpdateResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(request.groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        val groupUser =
            groupUserRepository.findByUserAndGroup(user, group)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)

        if (!groupUser.role.isTopAmin()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        val category =
            categoryRepository.findByIdOrNull(request.categoryId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_CATEGORY.message,
            )

        return category
            .apply {
                this.name = request.name
                this.isPublic = request.isPublic
            }.toCategoryUpdateResponse(group.id)
    }

    @Transactional
    fun delete(
        email: String,
        categoryId: Long,
    ) {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val category =
            categoryRepository.findByIdOrNull(categoryId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_CATEGORY.message,
            )

        val groupUser =
            groupUserRepository.findByUserAndGroup(user, category.group)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)

        if (!groupUser.role.isTopAmin()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        categoryRepository.delete(category)
    }
}
