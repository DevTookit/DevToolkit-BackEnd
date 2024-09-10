package com.project.api.web.dto.response

data class TokenResponse(
    val grantType: String = "Bearer",
    val accessToken: String,
    val refreshToken: String,
)
