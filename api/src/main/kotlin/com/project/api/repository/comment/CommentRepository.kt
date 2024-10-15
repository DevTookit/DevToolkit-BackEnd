package com.project.api.repository.comment

import com.project.core.domain.comment.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository: JpaRepository<Comment, Long> {
}