package com.project.core.domain.bookmark

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.Group
import com.project.core.domain.section.Section
import com.project.core.domain.user.User
import com.project.core.internal.BookmarkType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class Bookmark(
    val contentId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    val group: Group,
    @Enumerated(EnumType.STRING)
    val type: BookmarkType,
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    val section: Section,
) : BaseEntity()
