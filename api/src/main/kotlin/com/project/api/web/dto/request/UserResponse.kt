package com.project.api.web.dto.request

import com.project.core.domain.user.User

data class UserResponse(
    val email: String,
    val name: String,
    val img: String?,
    val phoneNumber: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        fun User.toUserResponse() =
            UserResponse(
                email = this.email,
                name = this.name,
                img = this.img,
                phoneNumber = this.phoneNumber,
                description = this.description,
                latitude = this.point.y,
                longitude = this.point.x,
            )
    }
}
