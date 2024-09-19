package com.project.api.web.dto.response

import com.project.core.domain.category.Category
import com.project.core.domain.category.CategoryNotification
import com.project.core.internal.CategoryNotificationType

data class CategoryNotificationUpdateResponse(
    val categoryId: Long?,
    val categoryNotificationId: Long?,
    val type: CategoryNotificationType,
) {
    companion object {
        fun CategoryNotification.toCategoryNotificationUpdateResponse(categoryId: Long?): CategoryNotificationUpdateResponse {
            return CategoryNotificationUpdateResponse(
                categoryId = categoryId,
                categoryNotificationId = this.id,
                type = this.type
            )
        }
    }
}