package com.project.api.web.dto.response

import com.project.core.domain.content.Content
import com.project.core.internal.ContentType

data class ContentSearchResponse(
    val sectionId: Long?,
    val contentId: Long?,
    val name: String,
    val languages: List<String>?,
    val writerImg: String?,
    val writerName: String,
    val createdDate: Long?,
    val size: Long?,
    val type: ContentType,
    var bookmarkId: Long? = null,
) {
    companion object {
        fun Content.toContentSearchResponse() =
            ContentSearchResponse(
                contentId = this.id,
                name = this.name,
                languages = this.languages.map { it.name },
                writerImg = this.groupUser.user.img,
                writerName = this.groupUser.name,
                createdDate = this.createdDate,
                size = this.size,
                type = this.type,
                sectionId = this.section.id,
            )
    }
}
