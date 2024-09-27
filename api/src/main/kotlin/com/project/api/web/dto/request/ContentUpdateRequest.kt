package com.project.api.web.dto.request

import com.project.core.internal.ContentType

data class ContentUpdateRequest(
    val contentId: Long,
    val name: String?,
    val languages: List<String>?,
    val skills: List<String>?,
    val content: String?,
    val description: String?,
    val type: ContentType,
)
