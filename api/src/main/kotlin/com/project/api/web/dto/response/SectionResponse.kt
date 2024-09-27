package com.project.api.web.dto.response

import com.project.core.domain.section.Section
import com.project.core.internal.SectionType

data class SectionResponse(
    val folderId: Long?,
    val name: String,
    val isPublic: Boolean?,
    val type: SectionType,
) {
    companion object {
        fun Section.toResponse() =
            SectionResponse(
                folderId = this.id,
                name = this.name,
                isPublic = this.isPublic,
                type = this.type,
            )
    }
}
