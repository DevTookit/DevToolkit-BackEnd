package com.project.api.web.dto.request

data class FolderAttachmentCreateRequest(
    val parentFolderId: Long,
    val groupId: Long,
)
