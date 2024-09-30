package com.project.core.domain.group

import com.project.core.domain.BaseEntity
import com.project.core.domain.content.Content
import com.project.core.domain.user.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class GroupFileAccessLog(
    // 공개된 그룹이면 그룹유저가 아니더라도 볼 수 있어서
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    val content: Content,
    @ManyToOne(fetch = FetchType.LAZY)
    val group: Group,
    var lastAccessAt: Long,
) : BaseEntity()
