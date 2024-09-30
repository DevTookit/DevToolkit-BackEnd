package com.project.core.domain.section

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.GroupUser
import com.project.core.internal.SectionNotificationType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class SectionNotification(
    @ManyToOne(fetch = FetchType.LAZY) val section: Section,
    @ManyToOne(fetch = FetchType.LAZY) val groupUser: GroupUser,
    @Enumerated(value = EnumType.STRING)
    var type: SectionNotificationType = SectionNotificationType.ALL,
) : BaseEntity()
