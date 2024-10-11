package com.project.api.web.dto.response

data class FolderCreateResponse(
    val id: Long?,
    val name: String,
    val attachments: MutableList<FolderAttachmentResponse> = mutableListOf(),
)
