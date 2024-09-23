package com.project.api.web.dto.response

import com.project.core.domain.content.Folder

data class FolderResponse(
    val parentId: Long?,
    val name: String?,
    val attachments: List<FolderAttachmentResponse>? = null,
    val subFolders: List<FolderSubResponse>? = null,
) {
    companion object {
        fun Folder.toResponse(
            attachments: List<FolderAttachmentResponse>? = null,
            subFolders: List<FolderSubResponse>? = null,
        ): FolderResponse =
            FolderResponse(
                parentId = this.id,
                name = this.name,
                attachments = attachments,
                subFolders = subFolders,
            )
    }
}
