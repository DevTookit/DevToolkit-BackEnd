package com.project.api.service

import com.project.api.repository.UserRepository
import com.project.api.web.dto.request.UserJoinRequest
import com.project.api.web.dto.request.UserLoginRequest
import com.project.api.web.dto.request.UserResponse
import com.project.api.web.dto.request.UserResponse.Companion.toUserResponse
import com.project.api.web.dto.response.UserLoginResponse
import com.project.api.web.dto.response.UserLoginResponse.Companion.toUserLoginResponse
import com.project.core.domain.user.User
import com.project.core.util.LocationUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun create(request: UserJoinRequest) {
        userRepository.existsByEmail(request.email).let {
            if (!it) {
                // 에러 처리
            }
        }

        userRepository.save(
            User(
                email = request.email,
                password = passwordEncoder.encode(UUID.randomUUID().toString()),
                name = request.name,
                img = request.img,
                phoneNumber = request.phoneNumber,
                description = request.description,
                point = LocationUtil.createPoint(request.latitude, request.longitude),
            ),
        )

        // 이메일 로직 필요
    }

    fun login(request: UserLoginRequest): UserLoginResponse? {
        userRepository.findByEmail(request.email)?.let {
            if (!passwordEncoder.matches(it.password, request.password)) {
                // 에러처리
            }
            return it.toUserLoginResponse(authService.createAccessToken(it.email))
        }

        return null
    }

    fun readMe(jwt: Jwt): UserResponse? {
        userRepository.findByEmail(jwt.subject)?.let {
            return it.toUserResponse()
        }

        return null
    }
}
