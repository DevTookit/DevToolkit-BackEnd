package com.project.api.web.dto.response

import com.project.core.domain.section.Section
import com.project.core.internal.SectionType

data class SectionCreateResponse(
    val groupId: Long?,
    val sectionId: Long?,
    val name: String,
    val isPublic: Boolean,
    val type: SectionType,
) {
    companion object {
        fun Section.toCategoryCreateResponse(groupId: Long?) =
            SectionCreateResponse(
                groupId = groupId,
                sectionId = this.id,
                name = this.name,
                isPublic = this.isPublic,
                type = this.type,
            )
    }
}
