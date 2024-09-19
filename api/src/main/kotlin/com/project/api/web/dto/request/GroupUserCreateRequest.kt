package com.project.api.web.dto.request

import com.project.core.internal.GroupRole

data class GroupUserCreateRequest(
    val groupId: Long,
    val name: String?,
    val role: GroupRole,
)
