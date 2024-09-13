package com.project.api.web.dto.request

import com.project.core.internal.GroupRole

data class GroupUserUpdateRequest(
    val groupId: Long,
    val role: GroupRole,
    val groupUserId: Long,
)
