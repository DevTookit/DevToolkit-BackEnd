package com.project.api.repository.group

import com.project.core.domain.group.Group
import com.project.core.domain.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface GroupRepository :
    JpaRepository<Group, Long>,
    QuerydslPredicateExecutor<Group> {
    fun findByUser(
        user: User,
        pageable: Pageable,
    ): List<Group>

    @Query("SELECT g FROM Group g WHERE g.isPublic = true ORDER BY SIZE(g.groupUsers) DESC")
    fun findAllByIsPublicIsTrueOrderByGroupUsersSizeDesc(pageable: Pageable): List<Group>

    fun findByUserEmail(
        email: String,
        pageable: Pageable,
    ): List<Group>
}
