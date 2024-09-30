package com.project.api.supprot.fixture

import com.project.api.repository.user.UserHashTagRepository
import com.project.core.domain.user.User
import com.project.core.domain.user.UserHashTag
import org.springframework.stereotype.Component

@Component
class UserHashTagFixture(
    private val userHashTagRepository: UserHashTagRepository,
) : Fixture {
    fun create(
        user: User,
        tags: List<String>,
    ) {
        userHashTagRepository.saveAll(
            tags.map {
                UserHashTag(
                    content = it,
                    user = user,
                )
            },
        )
    }

    override fun tearDown() {
        userHashTagRepository.deleteAll()
    }
}
