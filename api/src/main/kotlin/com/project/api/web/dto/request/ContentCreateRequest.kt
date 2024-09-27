package com.project.api.web.dto.request

import com.project.core.internal.ContentType

data class ContentCreateRequest(
    val name: String,
    val languages: List<String>,
    val skills: List<String>,
    val content: String,
    // 코드 설명
    val codeDescription: String?,
    val type: ContentType,
)
