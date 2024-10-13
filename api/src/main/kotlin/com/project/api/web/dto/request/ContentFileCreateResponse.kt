package com.project.api.web.dto.request

import com.project.core.domain.content.Content

data class ContentFileCreateResponse(
    val name: String,
    val id: Long?,
    val extension: String,
    val size: Long,
    val url: String,
    val createdDate: Long?,
    val lastModifiedDate: Long?,
    val sectionId: Long?,
) {
    companion object {
        fun Content.toContentFileCreateResponse(): ContentFileCreateResponse =
            ContentFileCreateResponse(
                name = name,
                id = id,
                extension = extension!!,
                size = size!!,
                url = url!!,
                createdDate = createdDate,
                lastModifiedDate = lastModifiedDate,
                sectionId = this.section.id,
            )
    }
}
