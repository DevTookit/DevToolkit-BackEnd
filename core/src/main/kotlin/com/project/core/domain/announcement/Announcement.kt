package com.project.core.domain.announcement

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class Announcement(
    @ManyToOne(fetch = FetchType.LAZY)
    val group: Group,
    @ManyToOne(fetch = FetchType.LAZY)
    val groupUser: GroupUser,
    var name: String,
    var content: String,
) : BaseEntity()
