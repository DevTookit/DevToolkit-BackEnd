package com.project.api.web.dto.response

import com.project.core.domain.section.Section

data class CategoryUpdateResponse(
    val groupId: Long?,
    val categoryId: Long?,
    val name: String,
    val isPublic: Boolean,
) {
    companion object {
        fun Section.toCategoryUpdateResponse(groupId: Long?): CategoryUpdateResponse =
            CategoryUpdateResponse(
                groupId = groupId,
                categoryId = this.id,
                name = this.name,
                isPublic = this.isPublic,
            )
    }
}
