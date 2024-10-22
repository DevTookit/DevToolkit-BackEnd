package com.project.api.web.dto.response

import com.project.core.domain.comment.Comment

data class CommentCreateResponse(
    val commentId: Long?,
    val content: String,
    val writerId: Long?,
    val writerName: String,
    val writerImg: String?,
    val mentionsIds: List<Long>?,
) {
    companion object {
        fun Comment.toCommentCreateResponse(): CommentCreateResponse =
            CommentCreateResponse(
                commentId = this.id,
                content = this.content,
                writerId = this.groupUser.id,
                writerName = this.groupUser.name,
                writerImg = this.groupUser.user.img,
                mentionsIds =
                    this.mentions.map {
                        it.groupUser.id!!
                    },
            )
    }
}