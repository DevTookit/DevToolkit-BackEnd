package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.UserRepository
import com.project.api.web.dto.request.UserJoinRequest
import com.project.api.web.dto.request.UserLoginRequest
import com.project.api.web.dto.request.UserResponse
import com.project.api.web.dto.request.UserResponse.Companion.toUserResponse
import com.project.api.web.dto.request.UserUpdateRequest
import com.project.api.web.dto.response.UserLoginResponse
import com.project.api.web.dto.response.UserLoginResponse.Companion.toUserLoginResponse
import com.project.core.domain.user.User
import com.project.core.util.LocationUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val mailService: MailService,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun create(request: UserJoinRequest) {
        userRepository.existsByEmail(request.email).let {
            if (it) {
                throw RestException.badRequest(ErrorMessage.INVALID_ENTITY.message)
            }
        }

        val tmpPassword = UUID.randomUUID().toString().substring(0, 10)
        userRepository
            .save(
                User(
                    email = request.email,
                    password = passwordEncoder.encode(tmpPassword),
                    name = request.name,
                    img = request.img,
                    phoneNumber = request.phoneNumber,
                    description = request.description,
                    point = LocationUtil.createPoint(request.latitude, request.longitude),
                ),
            ).also {
                mailService.sendTmpPassword(it.email, tmpPassword)
            }
    }

    @Transactional(noRollbackFor = [RestException::class])
    fun login(request: UserLoginRequest): UserLoginResponse {
        val user = userRepository.findByEmail(request.email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        if (!user.enabled) {
            throw RestException
                .badRequest(ErrorMessage.IMPOSSIBLE_LOGIN.message)
        } else if (!passwordEncoder.matches(request.password, user.password)) {
            user.failCount = ++user.failCount
            throw RestException
                .badRequest(ErrorMessage.NOT_MATCH_PASSWORD.message)
        }

        return user
            .apply { failCount = 0 }
            .toUserLoginResponse(authService.createAccessToken(user.email))
    }

    fun readMe(email: String): UserResponse {
        val user =
            userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        return user.toUserResponse()
    }

    @Transactional
    fun updatePassword(
        email: String,
        request: UserUpdateRequest,
    ): UserResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        if (passwordEncoder.matches(request.password, user.password)) {
            throw RestException.badRequest(ErrorMessage.NEW_PASSWORD_MATCH_OLD_PASSWORD.message)
        }

        user.password = passwordEncoder.encode(request.password)
        return user.toUserResponse()
    }
}
