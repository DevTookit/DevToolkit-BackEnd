package com.project.api.web.dto.response

import com.project.core.domain.content.Content
import com.project.core.domain.content.Folder
import com.project.core.internal.BookmarkType

data class BookmarkResponse(
    val bookmarkId: Long?,
    val contentId: Long?,
    val type: BookmarkType,
    val name: String,
    val sectionId: Long?,
    val writerId: Long?,
    val writerName: String,
    val writerImg: String?,
) {
    companion object {
        fun Content.toBookmarkResponse(bookmarkId: Long?) =
            BookmarkResponse(
                bookmarkId = bookmarkId,
                contentId = this.id,
                type = BookmarkType.valueOf(this.type.name),
                name = this.name,
                sectionId = this.section.id,
                writerId = this.groupUser.user.id,
                writerName = this.groupUser.name,
                writerImg = this.groupUser.user.img,
            )

        fun Folder.toBookmarkResponse(bookmarkId: Long?) =
            BookmarkResponse(
                bookmarkId = bookmarkId,
                contentId = this.id,
                type = BookmarkType.FOLDER,
                name = this.name,
                sectionId = this.section.id,
                writerId = this.groupUser.user.id,
                writerName = this.groupUser.name,
                writerImg = this.groupUser.user.img,
            )
    }
}
