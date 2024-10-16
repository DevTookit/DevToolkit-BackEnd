package com.project.api.web.dto.response

import com.project.core.domain.announcement.Announcement

data class AnnouncementResponse(
    val announceId: Long?,
    val writerName: String,
    val writerImg: String?,
    val name: String,
    val content: String,
    val createdDate: Long?,
    val commentCnt: Long,
) {
    companion object {
        fun Announcement.toResponse(commentCnt: Long): AnnouncementResponse =
            AnnouncementResponse(
                announceId = this.id,
                writerName = this.groupUser.name,
                writerImg = this.groupUser.user.img,
                name = this.name,
                content = this.content,
                createdDate = this.createdDate,
                commentCnt = commentCnt,
            )
    }
}
