package com.project.api.repository.category

import com.project.core.domain.group.GroupUser
import com.project.core.domain.section.Section
import com.project.core.domain.section.SectionNotification
import org.springframework.data.jpa.repository.JpaRepository

interface SectionNotificationRepository : JpaRepository<SectionNotification, Long> {
    fun findBySectionAndGroupUser(
        section: Section,
        groupUser: GroupUser,
    ): SectionNotification?
}
