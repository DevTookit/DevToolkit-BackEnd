package com.project.api.fixture

import com.project.api.repository.user.UserRepository
import com.project.core.domain.user.User
import com.project.core.util.LocationUtil
import org.locationtech.jts.geom.Point
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class UserFixture(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : Fixture {
    fun create(
        email: String = "test@test.com",
        password: String = "test",
        name: String = "name",
        img: String? = null,
        phoneNumber: String = "010-1234-1234",
        description: String = "description",
        point: Point = LocationUtil.createPoint(Random.nextDouble(139.0), Random.nextDouble(24.0)),
    ): User =
        userRepository.save(
            User(
                email = email,
                password = passwordEncoder.encode(password),
                name = name,
                img = img,
            ),
        )

    override fun tearDown() {
        userRepository.deleteAll()
    }
}
