package com.project.api.web.dto.request

data class GroupCreateRequest(
    val name: String,
    val img: String?,
    val description: String?,
    val isPublic: Boolean,
)
