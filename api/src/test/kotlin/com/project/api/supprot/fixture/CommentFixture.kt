package com.project.api.supprot.fixture

import com.project.api.repository.comment.CommentMentionRepository
import com.project.api.repository.comment.CommentRepository
import com.project.core.domain.comment.Comment
import com.project.core.domain.comment.CommentMention
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.internal.CommentType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CommentFixture(
    private val commentRepository: CommentRepository,
    private val commentMentionRepository: CommentMentionRepository,
) : Fixture {
    fun create(
        contentId: Long,
        group: Group,
        groupUser: GroupUser,
        content: String = UUID.randomUUID().toString(),
        type: CommentType = CommentType.entries.random(),
        mentions: List<CommentMention>? = null,
    ): Comment =
        commentRepository
            .save(
                Comment(
                    content = content,
                    group = group,
                    groupUser = groupUser,
                    type = type,
                    contentId = contentId,
                ),
            ).apply {
                mentions?.let {
                    this.mentions.addAll(it)
                }
            }

    override fun tearDown() {
        commentMentionRepository.deleteAll()
        commentRepository.deleteAll()
    }
}
