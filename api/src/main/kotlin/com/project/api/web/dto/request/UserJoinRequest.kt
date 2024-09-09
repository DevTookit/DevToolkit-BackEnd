package com.project.api.web.dto.request

import jakarta.validation.constraints.Email

data class UserJoinRequest(
    @field:Email
    val email: String,
    val name: String,
    val img: String?,
    val phoneNumber: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
)
