package com.project.api.repository.bookmark

import com.project.core.domain.bookmark.Bookmark
import com.project.core.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface BookmarkRepository :
    JpaRepository<Bookmark, Long>,
    QuerydslPredicateExecutor<Bookmark> {
    fun findByIdAndUser(
        id: Long,
        user: User,
    ): Bookmark?

    fun existsByContentIdAndUser(
        contentId: Long,
        user: User,
    ): Boolean
}
