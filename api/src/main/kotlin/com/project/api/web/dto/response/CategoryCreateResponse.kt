package com.project.api.web.dto.response

import com.project.core.domain.category.Category

data class CategoryCreateResponse(
    val groupId: Long?,
    val categoryId: Long?,
    val name: String,
    val isPublic: Boolean,
) {
    companion object {
        fun Category.toCategoryCreateResponse(groupId: Long?) =
            CategoryCreateResponse(
                groupId = groupId,
                categoryId = this.id,
                name = this.name,
                isPublic = this.isPublic,
            )
    }
}
