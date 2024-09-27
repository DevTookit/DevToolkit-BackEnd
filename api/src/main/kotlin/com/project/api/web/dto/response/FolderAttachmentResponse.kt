package com.project.api.web.dto.response

import com.project.core.domain.content.Content

data class FolderAttachmentResponse(
    val name: String,
    val id: Long?,
    val extension: String,
    val size: Long,
    val url: String,
    val createdAt: Long?,
    val lastModifiedDate: Long?,
) {
    companion object {
        fun Content.toResponse(): FolderAttachmentResponse =
            FolderAttachmentResponse(
                name = name,
                id = id,
                extension = extension!!,
                size = size!!,
                url = url!!,
                createdAt = createdDate,
                lastModifiedDate = lastModifiedDate,
            )
    }
}
