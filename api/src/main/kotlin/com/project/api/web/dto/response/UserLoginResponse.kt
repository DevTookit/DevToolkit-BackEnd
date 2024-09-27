package com.project.api.web.dto.response

import com.project.core.domain.user.User

data class UserLoginResponse(
    val token: TokenResponse,
    val email: String,
    val id: Long?,
    val isOnBoardingComplete: Boolean,
) {
    companion object {
        fun User.toUserLoginResponse(tokenResponse: TokenResponse) =
            UserLoginResponse(
                token = tokenResponse,
                email = email,
                id = id,
                isOnBoardingComplete = isOnBoardingComplete,
            )
    }
}
