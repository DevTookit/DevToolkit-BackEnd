package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.EmailForm
import com.project.api.internal.ErrorMessage
import com.project.api.repository.user.UserHashTagRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.UserCreateRequest
import com.project.api.web.dto.request.UserLoginRequest
import com.project.api.web.dto.request.UserResetPasswordRequest
import com.project.api.web.dto.request.UserResponse
import com.project.api.web.dto.request.UserResponse.Companion.toUserResponse
import com.project.api.web.dto.request.UserUpdateRequest
import com.project.api.web.dto.response.TokenResponse
import com.project.api.web.dto.response.UserLoginResponse
import com.project.api.web.dto.response.UserLoginResponse.Companion.toUserLoginResponse
import com.project.core.domain.user.User
import com.project.core.domain.user.UserHashTag
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userHashTagRepository: UserHashTagRepository,
    private val authService: AuthService,
    private val mailService: MailService,
    private val passwordEncoder: PasswordEncoder,
) {
    fun verifyEmail(
        email: String,
    ): String {
        val code = UUID.randomUUID().toString().substring(0, 10)
        mailService.send(email, code, EmailForm.VERIFY_EMAIL)
        return code
    }

    @Transactional
    fun create(request: UserCreateRequest) {
        userRepository.existsByEmail(request.email).let {
            if (it) {
                throw RestException.badRequest(ErrorMessage.INVALID_ENTITY.message)
            }
        }

        userRepository
            .save(
                User(
                    email = request.email,
                    password = passwordEncoder.encode(request.password),
                    name = request.name,
                    img = request.img,
                ).apply {
                    isVerified = true
                },
            ).also { user ->
                if (request.tags != null) {
                    userHashTagRepository.saveAll(
                        request.tags!!.map {
                            UserHashTag(
                                content = it.lowercase().replaceFirstChar { it.uppercase() },
                                user = user,
                            )
                        },
                    )
                }
            }
    }

    fun findEmail(email: String,): Boolean {
        return userRepository.existsByEmail(email)
    }

    @Transactional
    fun resetPassword(
        request: UserResetPasswordRequest,
    ) {
        val user = userRepository.findByEmail(request.email)
            ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        if(passwordEncoder.matches(request.newPassword, user.password)) {
            throw RestException.badRequest(ErrorMessage.IMPOSSIBLE_PASSWORD.message)
        }

        userRepository.save(
            user.apply { password = passwordEncoder.encode(request.newPassword) }
        )
    }

    @Transactional(noRollbackFor = [RestException::class])
    fun login(request: UserLoginRequest): UserLoginResponse {
        val user = userRepository.findByEmail(request.email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        if (!user.isEnabled) {
            throw RestException
                .badRequest(ErrorMessage.IMPOSSIBLE_LOGIN.message)
        } else if (!passwordEncoder.matches(request.password, user.password)) {
            user.failCount = ++user.failCount
            throw RestException
                .badRequest(ErrorMessage.NOT_MATCH_PASSWORD.message)
        }

        return user
            .apply { failCount = 0 }
            .toUserLoginResponse(authService.create(user.email))
    }

    @Transactional(readOnly = true)
    fun readMe(email: String): UserResponse {
        val user =
            userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        return user.toUserResponse()
    }

    @Transactional
    fun update(
        email: String,
        request: UserUpdateRequest,
    ): UserResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        updateUser(request, user)
        updateHashTags(request, user)

        return userRepository
            .save(user)
            .toUserResponse()
    }

    @Transactional
    fun createToken(email: String): TokenResponse = authService.create(email)

    private fun updateUser(
        request: UserUpdateRequest,
        user: User,
    ) {
        if (request.img == null) user.img = null else user.img = request.img

        request.name?.let {
            user.name = it
        }

        request.password?.let {
            if (passwordEncoder.matches(it, user.password)) {
                throw RestException.badRequest(ErrorMessage.NEW_PASSWORD_MATCH_OLD_PASSWORD.message)
            }
            user.password = passwordEncoder.encode(it)
        }
    }

    private fun updateHashTags(
        request: UserUpdateRequest,
        user: User,
    ) {
        request.tags?.let {
            userHashTagRepository.deleteAllByUserId(user.id!!)
            userHashTagRepository.saveAll(
                it.map {
                    UserHashTag(
                        content = it,
                        user = user,
                    )
                },
            )
        }
    }
}

