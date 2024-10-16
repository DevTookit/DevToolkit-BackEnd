package com.project.api.web.dto.response

import com.project.api.web.dto.response.CommentMentionResponse.Companion.toCommentMentionResponse
import com.project.core.domain.comment.Comment
import com.project.core.domain.comment.CommentMention

data class CommentReadResponse(
    val commentId: Long?,
    val writerId: Long?,
    val writerName: String,
    val writerImg: String?,
    val content: String,
    val mentions: List<CommentMentionResponse>?,
) {
    companion object {
        fun Comment.toCommentReadResponse() =
            CommentReadResponse(
                commentId = this.id,
                writerId = this.groupUser.id,
                writerName = this.groupUser.name,
                writerImg = this.groupUser.user.img,
                content = this.content,
                mentions =
                    this.mentions.map {
                        it.toCommentMentionResponse()
                    },
            )
    }
}

data class CommentMentionResponse(
    val groupUserId: Long?,
) {
    companion object {
        fun CommentMention.toCommentMentionResponse(): CommentMentionResponse =
            CommentMentionResponse(
                groupUserId = this.groupUser.id,
            )
    }
}
