package com.project.api.repository.category

import com.project.core.domain.category.Category
import com.project.core.domain.category.CategoryNotification
import com.project.core.domain.group.GroupUser
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryNotificationRepository : JpaRepository<CategoryNotification, Long> {
    fun findByCategoryAndGroupUser(
        category: Category,
        groupUser: GroupUser,
    ): CategoryNotification?
}
