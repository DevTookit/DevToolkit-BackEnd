package com.project.api.web.dto.request

import com.project.core.internal.CategoryNotificationType

data class CategoryNotificationUpdateRequest(
    val groupId: Long,
    val categoryId: Long,
    val type: CategoryNotificationType,
)
