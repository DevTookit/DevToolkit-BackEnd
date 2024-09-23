package com.project.api.web.dto.response

import com.project.core.domain.content.Folder

data class FolderSubResponse(
    val id: Long?,
    val name: String,
) {
    companion object {
        fun Folder.toFolderSubResponse() =
            FolderSubResponse(
                id = this.id,
                name = this.name,
            )
    }
}
