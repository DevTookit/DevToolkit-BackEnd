package com.project.api.web.dto.request

data class CategoryUpdateRequest(
    val name: String,
    val isPublic: Boolean,
    val categoryId: Long,
    val groupId: Long,
)
