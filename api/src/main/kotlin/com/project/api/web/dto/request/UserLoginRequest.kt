package com.project.api.web.dto.request

data class UserLoginRequest(
    val email: String,
    val password: String,
)
