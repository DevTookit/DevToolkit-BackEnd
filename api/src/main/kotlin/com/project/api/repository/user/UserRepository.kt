package com.project.api.repository.user

import com.project.core.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): User?
}
