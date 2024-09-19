package com.project.api.web.dto.response

import com.project.core.domain.group.GroupUser
import com.project.core.internal.GroupRole

data class GroupInvitationResponse(
    val groupName: String,
    val groupId: Long?,
    val groupUserId: Long?,
    val role: GroupRole,
) {
    companion object {
        fun GroupUser.toGroupInvitationResponse(): GroupInvitationResponse =
            GroupInvitationResponse(
                groupName = this.group.name,
                groupId = this.group.id,
                groupUserId = this.id,
                role = this.role,
            )
    }
}
