package com.project.api.web.dto.request

data class CategoryCreateRequest(
    val groupId: Long,
    val name: String,
    val isPublic: Boolean,
)
