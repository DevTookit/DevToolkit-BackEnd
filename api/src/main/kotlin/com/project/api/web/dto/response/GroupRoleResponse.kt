package com.project.api.web.dto.response

import com.project.core.domain.group.GroupUser
import com.project.core.internal.GroupRole

data class GroupRoleResponse(
    val groupId: Long?,
    val groupUserId: Long?,
    val role: GroupRole,
) {
    companion object {
        fun GroupUser.toGroupRoleResponse(groupId: Long?) =
            GroupRoleResponse(
                groupId = groupId,
                groupUserId = this.id,
                role = this.role,
            )
    }
}
