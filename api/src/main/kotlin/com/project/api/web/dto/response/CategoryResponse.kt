package com.project.api.web.dto.response

import com.project.core.domain.category.Category

data class CategoryResponse(
    val categoryId: Long?,
    val name: String,
    val isPublic: Boolean,
) {
    companion object {
        fun Category.toResponse() =
            CategoryResponse(
                categoryId = this.id,
                name = this.name,
                isPublic = this.isPublic,
            )
    }
}
