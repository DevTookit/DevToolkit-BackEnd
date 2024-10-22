package com.project.api.repository.comment

import com.project.core.domain.comment.Comment
import com.project.core.domain.comment.CommentMention
import org.springframework.data.jpa.repository.JpaRepository

interface CommentMentionRepository : JpaRepository<CommentMention, Long> {
    fun deleteByComment(comment: Comment)
}
