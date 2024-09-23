package com.project.api.web.dto.request

data class FolderAttachmentUpdateRequest(
    val name: String,
    val folderAttachmentId: Long,
    val groupId: Long,
)
