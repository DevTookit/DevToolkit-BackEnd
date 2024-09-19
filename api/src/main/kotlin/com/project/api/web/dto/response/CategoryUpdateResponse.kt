package com.project.api.web.dto.response

import com.project.core.domain.category.Category

data class CategoryUpdateResponse(
    val groupId: Long?,
    val categoryId: Long?,
    val name: String,
    val isPublic: Boolean,
) {
    companion object {
        fun Category.toCategoryUpdateResponse(groupId: Long?): CategoryUpdateResponse =
            CategoryUpdateResponse(
                groupId = groupId,
                categoryId = this.id,
                name = this.name,
                isPublic = this.isPublic,
            )
    }
}
