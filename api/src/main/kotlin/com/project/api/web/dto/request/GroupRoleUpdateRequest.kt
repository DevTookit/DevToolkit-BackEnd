package com.project.api.web.dto.request

import com.project.core.internal.GroupRole

data class GroupRoleUpdateRequest(
    val groupId: Long,
    val groupUserId: Long,
    val role: GroupRole,
)
