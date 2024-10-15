package com.project.api.service

import com.project.api.repository.comment.CommentMentionRepository
import com.project.api.repository.comment.CommentRepository
import com.project.api.repository.content.ContentRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.CommentCreateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val contentRepository: ContentRepository,
    private val commentRepository: CommentRepository,
    private val commentMentionRepository: CommentMentionRepository,
) {
    // 댓글은 그룹 유저만 달 수 있게??
    @Transactional
    fun create(
        email: String,
        request: CommentCreateRequest,
    ) {

    }
}