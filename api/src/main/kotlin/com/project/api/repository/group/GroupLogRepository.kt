package com.project.api.repository.group

import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupLog
import com.project.core.internal.ContentType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface GroupLogRepository : JpaRepository<GroupLog, Long> {
    fun findByGroup(
        group: Group,
        pageable: Pageable,
    ): List<GroupLog>

    fun findByGroupAndType(
        group: Group,
        type: ContentType,
        pageable: Pageable,
    ): List<GroupLog>
}
