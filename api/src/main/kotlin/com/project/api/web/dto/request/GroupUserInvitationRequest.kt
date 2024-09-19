package com.project.api.web.dto.request

data class GroupUserInvitationRequest(
    val groupId: Long,
    val userId: Long,
)
