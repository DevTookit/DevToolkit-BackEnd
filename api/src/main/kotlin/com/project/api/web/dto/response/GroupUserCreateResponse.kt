package com.project.api.web.dto.response

import com.project.core.domain.group.GroupUser
import com.project.core.internal.GroupRole

data class GroupUserCreateResponse(
    val groupId: Long?,
    val groupUserId: Long?,
    val role: GroupRole,
) {
    companion object {
        fun GroupUser.toGroupUserCreateResponse(groupId: Long?) =
            GroupUserCreateResponse(
                groupId = groupId,
                groupUserId = this.id,
                role = this.role,
            )
    }
}
