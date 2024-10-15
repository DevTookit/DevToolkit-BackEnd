package com.project.core.domain.comment

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.internal.CommentType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class Comment(
    val contentId: Long,
    @ManyToOne(fetch = FetchType.LAZY) val group: Group,
    @ManyToOne(fetch = FetchType.LAZY) val groupUser: GroupUser,
    val content: String,
    @Enumerated(EnumType.STRING) val type: CommentType,
) : BaseEntity()