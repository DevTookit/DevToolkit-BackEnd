package com.project.api.repository.group

import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.user.User
import com.project.core.internal.GroupRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface GroupUserRepository :
    JpaRepository<GroupUser, Long>,
    QuerydslPredicateExecutor<GroupUser> {
    fun findByUserAndGroup(
        user: User,
        group: Group,
    ): GroupUser?

    fun existsByUserAndGroup(
        user: User,
        group: Group,
    ): Boolean

    fun findByIdAndGroup(
        id: Long,
        group: Group,
    ): GroupUser?

    fun findByUserAndRole(
        user: User,
        role: GroupRole,
    ): List<GroupUser>
}
