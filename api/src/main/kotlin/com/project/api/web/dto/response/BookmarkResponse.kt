package com.project.api.web.dto.response

import com.project.core.domain.content.Content
import com.project.core.domain.content.Folder
import com.project.core.internal.BookmarkType

data class BookmarkResponse(
    val bookmarkId: Long?,
    val contentId: Long?,
    val type: BookmarkType,
    val name: String,
) {
    companion object {
        fun Content.toBookmarkResponse(bookmarkId: Long?) =
            BookmarkResponse(
                bookmarkId = bookmarkId,
                contentId = this.id,
                type = BookmarkType.valueOf(this.type.name),
                name = this.name,
            )

        fun Folder.toBookmarkResponse(bookmarkId: Long?) =
            BookmarkResponse(
                bookmarkId = bookmarkId,
                contentId = this.id,
                type = BookmarkType.FOLDER,
                name = this.name,
            )
    }
}
