package com.project.api.fixture

import com.project.api.repository.bookmark.BookmarkRepository
import com.project.core.domain.bookmark.Bookmark
import com.project.core.domain.group.Group
import com.project.core.domain.user.User
import com.project.core.internal.BookmarkType
import org.springframework.stereotype.Component

@Component
class BookmarkFixture(
    private val bookmarkRepository: BookmarkRepository,
) : Fixture {
    fun create(
        contentId: Long,
        group: Group,
        type: BookmarkType,
        user: User,
    ) = bookmarkRepository.save(
        Bookmark(
            contentId = contentId,
            group = group,
            type = type,
            user = user,
        ),
    )

    override fun tearDown() {
        bookmarkRepository.deleteAll()
    }
}
