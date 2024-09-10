package com.project.api.repository.group

import com.project.core.domain.group.GroupUser
import org.springframework.data.jpa.repository.JpaRepository

interface GroupUserRepository : JpaRepository<GroupUser, Long>
