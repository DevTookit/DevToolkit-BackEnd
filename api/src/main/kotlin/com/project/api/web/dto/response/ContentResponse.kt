package com.project.api.web.dto.response

import com.project.core.domain.content.Content
import com.project.core.internal.ContentType

data class ContentResponse(
    val contentId: Long?,
    val createdDate: Long?,
    val lastModifiedDate: Long?,
    val name: String,
    val writerName: String,
    val writerId: Long?,
    val type: ContentType,
    val content: String,
    val codeDescription: String?,
    val languages: List<String>?,
    val skills: List<String>?,
    val attachments: List<String>?,
    var isBookmark: Boolean = false,
) {
    companion object {
        fun Content.toResponse() =
            ContentResponse(
                contentId = this.id,
                createdDate = this.createdDate,
                lastModifiedDate = this.lastModifiedDate,
                name = this.name,
                writerId = this.groupUser.id,
                writerName = this.groupUser.name,
                type = this.type,
                content = this.content!!,
                codeDescription = this.codeDescription,
                languages = this.languages.map { it.name },
                skills = this.skills.map { it.name },
                attachments = this.attachments.map { it.name },
            )
    }

    fun bookmark(isBookmark: Boolean) = this.isBookmark == isBookmark
}
