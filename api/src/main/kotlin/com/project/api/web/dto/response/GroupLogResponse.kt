package com.project.api.web.dto.response

import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupLog
import com.project.core.internal.ContentType

data class GroupLogResponse(
    val creatorId: Long?,
    val creatorName: String,
    val creatorImg: String?,
    val logs: List<GroupLogDetailResponse>?,
) {
    companion object {
        fun Group.toGroupLogResponse(logs: List<GroupLogDetailResponse>): GroupLogResponse =
            GroupLogResponse(
                creatorId = this.user.id,
                creatorName = this.user.name,
                creatorImg = this.user.img,
                logs = logs,
            )
    }
}

data class GroupLogDetailResponse(
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
        fun GroupLog.toGroupLogDetailResponse(): GroupLogDetailResponse =
            GroupLogDetailResponse(
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
