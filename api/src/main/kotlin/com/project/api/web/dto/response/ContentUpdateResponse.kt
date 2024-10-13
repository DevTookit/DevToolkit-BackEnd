package com.project.api.web.dto.response

import com.project.core.domain.content.Content
import com.project.core.internal.ContentType

data class ContentUpdateResponse(
    val contentId: Long?,
    val name: String?,
    val languages: List<String>?,
    val skills: List<String>?,
    val attachments: List<String>?,
    val content: String?,
    val description: String?,
    val type: ContentType,
    val sectionId: Long?,
) {
    companion object {
        fun Content.toContentUpdateResponse(): ContentUpdateResponse =
            ContentUpdateResponse(
                contentId = this.id,
                name = this.name,
                content = this.content,
                description = this.codeDescription,
                type = this.type,
                languages = this.languages.map { it.name },
                skills = this.skills.map { it.name },
                attachments = this.attachments.map { it.name },
                sectionId = this.section.id,
            )
    }
}
