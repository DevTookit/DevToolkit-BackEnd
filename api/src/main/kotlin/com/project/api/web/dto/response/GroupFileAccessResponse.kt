package com.project.api.web.dto.response

import com.project.core.domain.group.GroupFileAccessLog

data class GroupFileAccessResponse(
    val contentId: Long?,
    val lastAccessAt: Long,
    val name: String,
    val extension: String?,
    val folderId: Long?,
    val size: Long?,
) {
    companion object {
        fun GroupFileAccessLog.toGroupFileAccessResponse(): GroupFileAccessResponse =
            GroupFileAccessResponse(
                contentId = this.content.id,
                lastAccessAt = this.lastAccessAt,
                name = this.content.name,
                extension = this.content.extension,
                folderId = this.content.parentFolder?.id,
                size = this.content.size,
            )
    }
}
