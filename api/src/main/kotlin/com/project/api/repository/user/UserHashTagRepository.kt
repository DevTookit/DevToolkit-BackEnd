package com.project.api.repository.user

import com.project.core.domain.user.UserHashTag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserHashTagRepository : JpaRepository<UserHashTag, Long> {
    @Modifying
    @Query("DELETE FROM UserHashTag uht WHERE uht.user.id = :userId")
    fun deleteAllByUserId(
        @Param("userId") id: Long,
    )
}
