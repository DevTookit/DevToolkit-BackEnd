package com.project.api.web.dto.request

data class UserJoinRequest(
    val email: String,
    val name: String,
    val img: String?,
    val phoneNumber: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
)
