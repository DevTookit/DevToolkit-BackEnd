package com.project.core.domain.comment

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.GroupUser
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class CommentMention(
    @ManyToOne(fetch = FetchType.LAZY) val comment: Comment,
    @ManyToOne(fetch = FetchType.LAZY) var groupUser: GroupUser,
) : BaseEntity() {
}