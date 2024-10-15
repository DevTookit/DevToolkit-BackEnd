package com.project.api.web.dto.request

import com.project.core.internal.CommentType

data class CommentCreateRequest(
    val groupId: Long,
    val contentId: Long,
    val content: String,
    // TODO 멘션
    val mentions: List<CommentCreateMentionRequest>?,
    val type: CommentType,
)

data class CommentCreateMentionRequest(
    val groupUserId: Long,
)
