package com.project.api.web.dto.response

import com.project.core.domain.group.Group

data class GroupResponse(
    val id: Long?,
    val name: String,
    val img: String?,
    val description: String?,
    val isPublic: Boolean,
) {
    companion object {
        fun Group.toResponse(): GroupResponse =
            GroupResponse(
                id = this.id,
                name = this.name,
                img = this.img,
                description = this.description,
                isPublic = this.isPublic,
            )
    }
}
