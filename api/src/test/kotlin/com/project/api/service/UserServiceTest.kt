package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.repository.user.UserRepository
import com.project.api.supprot.container.RedisTestContainer
import com.project.api.supprot.fixture.UserFixture
import com.project.api.supprot.fixture.UserHashTagFixture
import com.project.api.web.dto.request.UserCreateRequest
import com.project.api.web.dto.request.UserLoginRequest
import com.project.api.web.dto.request.UserResetPasswordRequest
import com.project.api.web.dto.request.UserUpdateRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

class UserServiceTest(
    @Autowired private val userService: UserService,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val userHashTagFixture: UserHashTagFixture,
    @Autowired private val userRepository: UserRepository,
): TestCommonSetting()  {
    @AfterEach
    fun tearDown() {
        userHashTagFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun create() {
        val userCreateRequest =
            UserCreateRequest(
                email = "test@test.com",
                password = "test",
                name = "test",
                tags = null,
                job = "Engineering",
            )

        userService.create(userCreateRequest, null)

        val response = userRepository.findAll()

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].email).isEqualTo(userCreateRequest.email)
        Assertions.assertThat(response[0].name).isEqualTo(userCreateRequest.name)
        Assertions.assertThat(response[0].job).isEqualTo(userCreateRequest.job)
    }

    @Test
    fun verifyEmailAlreadyVerified() {
        val user = userFixture.create()

        Assertions
            .assertThatThrownBy {
                userService.verifyEmail(user.email)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun resetPassword() {
        val user = userFixture.create()
        val request =
            UserResetPasswordRequest(
                email = user.email,
                newPassword = user.password + "1234",
            )

        userService.resetPassword(request)
    }

    @Test
    fun resetPasswordImpossible() {
        val password = "dasdfjawoeifdvs"
        val user = userFixture.create(password = password)
        val request =
            UserResetPasswordRequest(
                email = user.email,
                newPassword = password,
            )

        Assertions
            .assertThatThrownBy {
                userService.resetPassword(request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun login() {
        val password = "password"
        val user = userFixture.create(password = password)

        val response =
            userService.login(
                UserLoginRequest(
                    email = user.email,
                    password = password,
                ),
            )

        Assertions.assertThat(response.email).isEqualTo(user.email)
        Assertions.assertThat(response.id).isEqualTo(user.id)
    }

    @Test
    fun readMe() {
        val user = userFixture.create()
        val tags = listOf("Java", "Kotlin")
        userHashTagFixture.create(
            user = user,
            tags = tags,
        )
        val response = userService.readMe(user.email)

        Assertions.assertThat(response.email).isEqualTo(user.email)
        Assertions.assertThat(response.img).isEqualTo(user.img)
        Assertions.assertThat(response.name).isEqualTo(user.name)
        Assertions.assertThat(response.tags).containsAll(tags)
    }

    @Test
    fun update() {
        val user = userFixture.create()
        val tags = listOf("Python")
        val request =
            UserUpdateRequest(
                name = "hello",
                tags = tags,
                job = "Front",
            )
        val response =
            userService.update(
                user.email,
                request,
                null,
            )

        Assertions.assertThat(response.email).isEqualTo(user.email)
        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.tags).containsAll(tags)
    }

    @Test
    fun checkOnBoarding() {
        val user =
            userFixture.create(
                isOnBoardingComplete = false,
            )

        val response = userService.checkOnBoarding(user.email)

        Assertions.assertThat(response).isEqualTo(user.isOnBoardingComplete)
    }

    @Test
    fun updateOnBoarding() {
        val user =
            userFixture.create(
                isOnBoardingComplete = false,
            )

        userService.updateOnBoarding(user.email, true)

        val response = userRepository.findAll()
        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].email).isEqualTo(user.email)
        Assertions.assertThat(response[0].isOnBoardingComplete).isTrue()
    }
}
