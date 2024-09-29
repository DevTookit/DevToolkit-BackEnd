package com.project.job.repository

import com.project.core.domain.group.GroupUser
import org.springframework.data.jpa.repository.JpaRepository

interface GroupUserRepository : JpaRepository<GroupUser, Long>
