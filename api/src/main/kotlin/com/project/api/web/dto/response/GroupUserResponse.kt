package com.project.api.web.dto.response

import com.project.core.domain.group.GroupUser
import com.project.core.internal.GroupRole

data class GroupUserResponse(
    val groupUserId: Long?,
    val role: GroupRole,
    val name: String,
) {
    companion object {
        fun GroupUser.toGroupUserResponse() =
            GroupUserResponse(
                groupUserId = this.id,
                role = this.role,
                name = this.name,
            )
    }
}
