package com.project.api.web.dto.response

import com.project.core.domain.section.Section

data class FolderUpdateResponse(
    val folderId: Long?,
    val name: String,
) {
    companion object {
        fun Section.toFolderUpdateResponse(): FolderUpdateResponse =
            FolderUpdateResponse(
                folderId = this.id,
                name = this.name,
            )
    }
}
