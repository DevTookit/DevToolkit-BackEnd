package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.external.FileService
import com.project.api.external.MailService
import com.project.api.internal.EmailForm
import com.project.api.internal.ErrorMessage
import com.project.api.internal.FilePath
import com.project.api.repository.user.UserHashTagRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.UserCreateRequest
import com.project.api.web.dto.request.UserLoginRequest
import com.project.api.web.dto.request.UserResetPasswordRequest
import com.project.api.web.dto.request.UserUpdateRequest
import com.project.api.web.dto.response.TokenResponse
import com.project.api.web.dto.response.UserLoginResponse
import com.project.api.web.dto.response.UserLoginResponse.Companion.toUserLoginResponse
import com.project.api.web.dto.response.UserResponse
import com.project.api.web.dto.response.UserResponse.Companion.toUserResponse
import com.project.core.domain.user.User
import com.project.core.domain.user.UserHashTag
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userHashTagRepository: UserHashTagRepository,
    private val authService: AuthService,
    private val mailService: MailService,
    private val passwordEncoder: PasswordEncoder,
    private val fileService: FileService,
    private val redisService: RedisService,
) {
    @Transactional
    fun create(
        request: UserCreateRequest,
        img: MultipartFile?,
    ) {
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
                    img =
                        img?.let {
                            fileService.upload(it, FilePath.PROFILE.name).url
                        },
                    job = request.job,
                ),
            ).also { user ->
                if (request.tags != null) {
                    userHashTagRepository.saveAll(
                        request.tags.map {
                            UserHashTag(
                                content = it.lowercase().replaceFirstChar { it.uppercase() },
                                user = user,
                            )
                        },
                    )
                }
            }
    }

    fun verifyEmail(email: String) {
        val user =
            userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        if (user.isVerified) {
            throw RestException.badRequest(ErrorMessage.ALREADY_VERIFIED_EMAIL.message)
        }
        val code = UUID.randomUUID().toString().substring(0, 10)
        mailService.send(email, code, EmailForm.VERIFY_EMAIL)
        redisService.add(makeVerifyKey(email), code, 180)
    }

    @Transactional
    fun updateVerifyEmail(
        email: String,
        code: String,
    ): Boolean {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val verifyCode =
            redisService.get(makeVerifyKey(email))
                ?: throw RestException.badRequest(ErrorMessage.NOT_EXIST_CODE.message)

        if (!verifyCode.toString().replace("\"", "").equals(code)) {
            return false
        }

        user.apply {
            this.isVerified = true
        }
        return true
    }

    fun findEmail(email: String): Boolean = userRepository.existsByEmail(email)

    @Transactional
    fun resetPassword(request: UserResetPasswordRequest) {
        val user =
            userRepository.findByEmail(request.email)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        if (passwordEncoder.matches(request.newPassword, user.password)) {
            throw RestException.badRequest(ErrorMessage.IMPOSSIBLE_PASSWORD.message)
        }

        userRepository.save(
            user.apply {
                password = passwordEncoder.encode(request.newPassword)
                this.isEnabled = true
                this.failCount = 0
            },
        )
    }

    @Transactional(noRollbackFor = [RestException::class])
    fun login(request: UserLoginRequest): UserLoginResponse {
        val user = userRepository.findByEmail(request.email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        validateLogin(user, request)

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
        img: MultipartFile?,
    ): UserResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        updateUser(request, user, img)
        updateHashTags(request, user)

        return userRepository
            .save(user)
            .toUserResponse()
    }

    @Transactional
    fun createToken(
        email: String,
        refreshToken: String,
    ): TokenResponse {
        val validate = authService.validate(email, refreshToken)
        if (!validate) {
            throw RestException.forbidden(ErrorMessage.INCORRECT_REFRESH_TOKEN.message)
        }
        return authService.create(email)
    }

    fun readOne(userId: Long): UserResponse {
        val user =
            userRepository.findByIdOrNull(userId) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        return user.toUserResponse()
    }

    fun checkOnBoarding(email: String): Boolean {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        return user.isOnBoardingComplete
    }

    @Transactional
    fun updateOnBoarding(
        email: String,
        isOnBoarding: Boolean,
    ) {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        user.apply {
            isOnBoardingComplete = isOnBoarding
        }
    }

    private fun validateLogin(
        user: User,
        request: UserLoginRequest,
    ) {
        val errorMessage =
            when {
                !user.isVerified -> ErrorMessage.NOT_EMAIL_VERIFIED.message
                !user.isEnabled -> ErrorMessage.CONTACT_ADMIN.message
                !passwordEncoder.matches(request.password, user.password) -> {
                    user.failCount = ++user.failCount
                    if (user.failCount >= 5) {
                        user.isEnabled = false
                        ErrorMessage.NOT_MATCH_PASSWORD_MAX_REACHED.message
                    }
                    ErrorMessage.NOT_MATCH_PASSWORD.message
                }

                else -> null
            }
        errorMessage?.let { throw RestException.authorized(it) }
    }

    private fun makeVerifyKey(email: String) = "verify_$email"

    private fun updateUser(
        request: UserUpdateRequest,
        user: User,
        img: MultipartFile?,
    ) {
        img?.let {
            user.img = fileService.upload(it, FilePath.PROFILE.name).url
        }

        request.name?.let {
            user.name = it
        }

        request.job?.let {
            user.job = it
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
