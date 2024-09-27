package com.project.api.web.dto.request

data class FolderCreateRequest(
    val parentFolderId: Long?,
    val name: String,
)
