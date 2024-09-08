package com.project.api.web.dto.response

import com.project.core.domain.user.User

data class UserLoginResponse(
    val accessToken: String,
    val email: String,
    val id: Long?,
) {
    companion object {
        fun User.toUserLoginResponse(accessToken: String) =
            UserLoginResponse(
                accessToken = accessToken,
                email = email,
                id = id,
            )
    }
}
