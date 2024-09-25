package com.project.api.web.dto.response

import com.project.core.domain.content.Content
import com.project.core.internal.ContentType

data class ContentCreateResponse(
    val contentId: Long?,
    val name: String,
    val type: ContentType,
) {
    companion object {
        fun Content.toContentCreateResponse() =
            ContentCreateResponse(
                contentId = this.id,
                name = this.title,
                type = this.type,
            )
    }
}
