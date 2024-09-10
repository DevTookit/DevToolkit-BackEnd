package com.project.api.web.dto.response

import com.project.core.domain.group.GroupUser
import com.project.core.internal.GroupRole

data class GroupRoleUpdateResponse(
    val groupUserId: Long?,
    val groupId: Long,
    val role: GroupRole,
) {
    companion object {
        fun GroupUser.toGroupRoleUpdateResponse(groupId: Long) =
            GroupRoleUpdateResponse(
                groupId = groupId,
                groupUserId = this.id,
                role = this.role,
            )
    }
}
