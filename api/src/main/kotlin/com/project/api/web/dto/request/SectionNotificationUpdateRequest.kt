package com.project.api.web.dto.request

import com.project.core.internal.CategoryNotificationType

data class SectionNotificationUpdateRequest(
    val groupId: Long,
    val sectionId: Long,
    val type: CategoryNotificationType,
)
