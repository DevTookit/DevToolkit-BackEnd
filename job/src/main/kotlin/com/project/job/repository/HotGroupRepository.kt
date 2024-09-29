package com.project.job.repository

import com.project.core.domain.group.Group
import com.project.core.domain.statistics.HotGroup
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface HotGroupRepository : JpaRepository<HotGroup, Long> {
    fun findByGroup(group: Group): HotGroup?

    @Query("SELECT h FROM HotGroup h ORDER BY (h.visitCnt + h.userCnt + h.joinCnt) DESC")
    fun findTopBySumOfCounts(pageable: Pageable): List<HotGroup>
}
