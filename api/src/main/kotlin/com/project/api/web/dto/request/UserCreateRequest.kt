package com.project.api.web.dto.request

import jakarta.validation.constraints.Email

data class UserCreateRequest(
    @field:Email
    val email: String,
    val name: String,
    val img: String?,
    val tags: List<String>,
)
