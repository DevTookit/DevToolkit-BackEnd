package com.project.api.service

import com.project.api.fixture.UserFixture
import com.project.api.fixture.UserHashTagFixture
import com.project.api.web.dto.request.UserLoginRequest
import com.project.api.web.dto.request.UserUpdateRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest(
    @Autowired private val userService: UserService,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val userHashTagFixture: UserHashTagFixture,
) {
    @AfterEach
    fun tearDown() {
        userHashTagFixture.tearDown()
        userFixture.tearDown()
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
                password = "hello1233",
                name = "hello",
                img = null,
                tags = tags,
            )
        val response =
            userService.update(
                user.email,
                request,
            )

        Assertions.assertThat(response.email).isEqualTo(user.email)
        Assertions.assertThat(response.img).isEqualTo(request.img)
        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.tags).containsAll(tags)
    }
}
