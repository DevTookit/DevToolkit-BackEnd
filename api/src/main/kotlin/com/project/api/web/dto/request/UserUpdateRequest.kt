package com.project.api.web.dto.request

data class UserUpdateRequest(
    val name: String?,
    val tags: List<String>?,
    val job: String?,
)
