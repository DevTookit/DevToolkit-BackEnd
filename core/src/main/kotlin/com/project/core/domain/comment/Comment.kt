package com.project.core.domain.comment

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.internal.CommentType
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Comment(
    val contentId: Long,
    @ManyToOne(fetch = FetchType.LAZY) val group: Group,
    @ManyToOne(fetch = FetchType.LAZY) val groupUser: GroupUser,
    var content: String,
    @Enumerated(EnumType.STRING) val type: CommentType,
) : BaseEntity() {
    @OneToMany(mappedBy = "comment", fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val mentions: MutableSet<CommentMention> = mutableSetOf()
}
