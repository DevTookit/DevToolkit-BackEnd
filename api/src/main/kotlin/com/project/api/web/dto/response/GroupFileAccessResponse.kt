package com.project.api.web.dto.response

import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupFileAccessLog

data class GroupFileAccessResponse(
    val creatorId: Long?,
    val creatorName: String,
    val creatorImg: String?,
    val logs: List<GroupFileAccessDetailResponse>,
) {
    companion object {
        fun Group.toGroupFileAccessResponse(logs: List<GroupFileAccessDetailResponse>): GroupFileAccessResponse =
            GroupFileAccessResponse(
                creatorId = this.user.id,
                creatorName = this.user.name,
                creatorImg = this.user.img,
                logs = logs,
            )
    }
}

data class GroupFileAccessDetailResponse(
    val contentId: Long?,
    val lastAccessAt: Long,
    val name: String,
    val extension: String?,
    val folderId: Long?,
    val size: Long?,
) {
    companion object {
        fun GroupFileAccessLog.toGroupFileAccessDetailResponse(): GroupFileAccessDetailResponse =
            GroupFileAccessDetailResponse(
                contentId = this.content.id,
                lastAccessAt = this.lastAccessAt,
                name = this.content.name,
                extension = this.content.extension,
                folderId = this.content.parentFolder?.id,
                size = this.content.size,
            )
    }
}
