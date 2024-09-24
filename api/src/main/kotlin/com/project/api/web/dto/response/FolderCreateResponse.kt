package com.project.api.web.dto.response

import com.project.core.domain.content.Folder

data class FolderCreateResponse(
    val id: Long?,
    val name: String,
    val attachments: MutableList<FolderAttachmentResponse> = mutableListOf(),
) {
    companion object {
        fun Folder.toFolderCreateResponse(attachments: List<FolderAttachmentResponse>? = null) =
            FolderCreateResponse(
                id = this.id,
                name = this.name,
            ).apply {
                attachments?.let {
                    this.attachments.addAll(it)
                }
            }
    }
}
