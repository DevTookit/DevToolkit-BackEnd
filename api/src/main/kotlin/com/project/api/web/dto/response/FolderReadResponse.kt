package com.project.api.web.dto.response

import com.project.api.internal.FolderReadType
import com.project.core.domain.content.Content
import com.project.core.domain.content.Folder

data class FolderReadResponse(
    val id: Long?,
    val name: String,
    val createdAt: Long?,
    val lastModifiedDate: Long?,
    val extension: String? = null,
    val size: Long? = null,
    val url: String? = null,
    val type: FolderReadType,
) {
    companion object {
        fun Content.toFolderReadResponse(): FolderReadResponse =
            FolderReadResponse(
                name = name,
                id = id,
                extension = extension!!,
                size = size!!,
                url = url!!,
                createdAt = createdDate,
                lastModifiedDate = lastModifiedDate,
                type = FolderReadType.FILE,
            )

        fun Folder.toFolderReadResponse() =
            FolderReadResponse(
                id = this.id,
                name = this.name,
                lastModifiedDate = this.lastModifiedDate,
                createdAt = createdDate,
                type = FolderReadType.FOLDER,
            )
    }
}
