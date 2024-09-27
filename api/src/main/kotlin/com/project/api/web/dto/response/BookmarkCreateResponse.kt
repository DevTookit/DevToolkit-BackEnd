package com.project.api.web.dto.response

import com.project.core.domain.bookmark.Bookmark
import com.project.core.internal.BookmarkType

data class BookmarkCreateResponse(
    val bookmarkId: Long?,
    val type: BookmarkType,
) {
    companion object {
        fun Bookmark.toBookmarkCreateResponse() =
            BookmarkCreateResponse(
                bookmarkId = this.id,
                type = this.type,
            )
    }
}
