package com.project.api.web.dto.response

import com.project.core.domain.group.GroupLog
import com.project.core.internal.ContentType

data class GroupLogResponse(
    val id: Long?,
    val writerName: String,
    val writerId: Long?,
    val writerImg: String?,
    val type: ContentType,
    val createdAt: Long?,
    val contentName: String,
    val contentId: Long?,
    val sectionId: Long?,
) {
    companion object {
        fun GroupLog.toGroupLogResponse(): GroupLogResponse =
            GroupLogResponse(
                id = this.id,
                writerName = this.user.name,
                writerId = this.user.id,
                writerImg = this.user.img,
                type = this.type,
                createdAt = this.createdDate,
                contentName = this.contentName,
                contentId = this.contentId,
                sectionId = this.sectionId,
            )
    }
}
