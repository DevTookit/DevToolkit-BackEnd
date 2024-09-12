package com.project.api.repository.group

import com.project.core.domain.group.Group
import org.springframework.data.jpa.repository.JpaRepository

interface GroupRepository : JpaRepository<Group, Long>
