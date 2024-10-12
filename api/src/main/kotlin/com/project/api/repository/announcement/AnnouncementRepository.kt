package com.project.api.repository.announcement

import com.project.core.domain.announcement.Announcement
import com.project.core.domain.group.Group
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface AnnouncementRepository : JpaRepository<Announcement, Long> {
    fun findByGroup(
        group: Group,
        pageable: Pageable,
    ): List<Announcement>

    fun findByIdAndGroup(
        id: Long,
        group: Group,
    ): Announcement?

    fun deleteByIdAndGroup(
        id: Long,
        group: Group,
    )
}
