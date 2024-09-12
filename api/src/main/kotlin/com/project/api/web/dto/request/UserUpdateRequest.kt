package com.project.api.web.dto.request

data class UserUpdateRequest(
    val password: String?,
    val name: String?,
    val img: String?,
    val tags: List<String>?,
)
