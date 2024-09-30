package com.project.api.supprot.fixture

import com.project.api.repository.user.UserRepository
import com.project.core.domain.user.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserFixture(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : Fixture {
    fun create(
        email: String = UUID.randomUUID().toString() + "@gmail.com",
        password: String = "test",
        name: String = UUID.randomUUID().toString(),
        img: String? = null,
        isVerified: Boolean = true,
        isOnBoardingComplete: Boolean = true,
    ): User =
        userRepository.save(
            User(
                email = email,
                password = passwordEncoder.encode(password),
                name = name,
                img = img,
                job = null,
            ).apply {
                this.isVerified = isVerified
                this.isOnBoardingComplete = isOnBoardingComplete
            },
        )

    override fun tearDown() {
        userRepository.deleteAll()
    }
}
