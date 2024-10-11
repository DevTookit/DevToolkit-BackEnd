package com.project.api.web.dto.response

import com.project.api.internal.FolderReadType

data class FolderReadResponse(
    val id: Long?,
    val name: String,
    val createdAt: Long?,
    val lastModifiedDate: Long?,
    val extension: String? = null,
    val size: Long? = null,
    val url: String? = null,
    val type: FolderReadType,
)
