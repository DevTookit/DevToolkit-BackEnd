package com.project.api.web.dto.request

data class CommentUpdateRequest(
    val groupId: Long,
    val contentId: Long,
    val commentId: Long,
    val content: String,
    val mentions: List<CommentCreateMentionRequest>?,
)
