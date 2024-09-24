package com.project.core.domain.notification

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.Group
import com.project.core.domain.section.Section
import com.project.core.domain.user.User
import com.project.core.internal.NotificationType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class Notification(
    @ManyToOne(fetch = FetchType.LAZY) val user: User,
    @Enumerated(value = EnumType.STRING) val type: NotificationType,
    @ManyToOne(fetch = FetchType.LAZY) val group: Group,
    val contentId: Long,
) : BaseEntity() {
    var isRead = false

    @ManyToOne(fetch = FetchType.LAZY)
    var section: Section? = null
}
