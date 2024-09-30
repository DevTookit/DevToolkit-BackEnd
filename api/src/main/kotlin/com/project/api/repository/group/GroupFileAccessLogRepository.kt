package com.project.api.repository.group

import com.project.core.domain.content.Content
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupFileAccessLog
import com.project.core.domain.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface GroupFileAccessLogRepository : JpaRepository<GroupFileAccessLog, Long> {
    fun findByContentAndUser(
        content: Content,
        user: User,
    ): GroupFileAccessLog?

    fun findByUserAndGroup(
        user: User,
        group: Group,
        pageable: Pageable,
    ): List<GroupFileAccessLog>
}
