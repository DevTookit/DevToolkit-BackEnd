package com.project.api.supprot.fixture

import com.project.api.repository.comment.CommentMentionRepository
import com.project.core.domain.comment.Comment
import com.project.core.domain.comment.CommentMention
import com.project.core.domain.group.GroupUser
import org.springframework.stereotype.Component

@Component
class CommentMentionFixture(
    private val commentMentionRepository: CommentMentionRepository,
) : Fixture {
    fun create(
        comment: Comment,
        groupUser: GroupUser,
    ): CommentMention =
        commentMentionRepository.save(
            CommentMention(
                comment = comment,
                groupUser = groupUser,
            ),
        )

    override fun tearDown() {
        commentMentionRepository.deleteAll()
    }
}
