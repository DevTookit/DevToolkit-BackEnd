package com.project.api.repository.group

import com.project.core.domain.group.Group
import com.project.core.domain.statistics.HotGroup
import org.springframework.data.jpa.repository.JpaRepository

interface HotGroupRepository : JpaRepository<HotGroup, Long> {
    fun deleteByGroup(group: Group): Long
}
