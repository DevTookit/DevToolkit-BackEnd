package com.project.api.web.dto.response

import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser

data class GroupResponse(
    val id: Long?,
    val name: String,
    val img: String?,
    val creatorId: Long?,
    val creatorName: String,
    val creatorImg: String?,
    val description: String?,
    val isPublic: Boolean,
    val userCnt: Long? = null,
) {
    companion object {
        fun Group.toResponse(): GroupResponse =
            GroupResponse(
                id = this.id,
                name = this.name,
                img = this.img,
                description = this.description,
                isPublic = this.isPublic,
                userCnt = this.groupUsers.size.toLong(),
                creatorId = this.user.id,
                creatorName = this.user.name,
                creatorImg = this.user.img,
            )

        fun GroupUser.toGroupResponse(): GroupResponse =
            GroupResponse(
                id = this.group.id,
                name = this.group.name,
                img = this.group.img,
                description = this.group.description,
                isPublic = this.group.isPublic,
                creatorId = this.group.user.id,
                creatorName = this.group.user.name,
                creatorImg = this.group.user.img,
            )
    }
}
