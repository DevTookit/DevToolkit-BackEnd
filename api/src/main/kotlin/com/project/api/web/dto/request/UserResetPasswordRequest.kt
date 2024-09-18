package com.project.api.web.dto.request

data class UserResetPasswordRequest(
    val email: String,
    val newPassword: String,
)
