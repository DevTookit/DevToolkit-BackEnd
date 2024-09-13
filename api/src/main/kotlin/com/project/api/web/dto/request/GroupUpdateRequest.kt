package com.project.api.web.dto.request

data class GroupUpdateRequest(
    val id: Long,
    val name: String,
    val img: String?,
    val description: String?,
    val isPublic: Boolean,
)