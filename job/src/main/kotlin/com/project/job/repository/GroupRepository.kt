package com.project.job.repository

import com.project.core.domain.group.Group
import org.springframework.data.jpa.repository.JpaRepository

interface GroupRepository : JpaRepository<Group, Long> {
    fun findAllByIsPublic(isPublic: Boolean): List<Group>
}
