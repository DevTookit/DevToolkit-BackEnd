package com.project.api.web.dto.response

data class FolderResponse(
    val parentId: Long?,
    val name: String?,
    val lists: List<FolderReadResponse>?,
)
