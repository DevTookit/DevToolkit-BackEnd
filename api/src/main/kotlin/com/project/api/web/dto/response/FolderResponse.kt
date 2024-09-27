package com.project.api.web.dto.response

import com.project.core.domain.content.Folder

data class FolderResponse(
    val parentId: Long?,
    val name: String?,
    val lists: List<FolderReadResponse>?,
) {
    companion object {
        fun Folder.toResponse(lists: List<FolderReadResponse>? = null): FolderResponse =
            FolderResponse(
                parentId = this.id,
                name = this.name,
                lists = lists,
            )
    }
}
