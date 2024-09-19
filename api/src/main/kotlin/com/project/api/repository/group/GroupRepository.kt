package com.project.api.repository.group

import com.project.core.domain.group.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface GroupRepository :
    JpaRepository<Group, Long>,
    QuerydslPredicateExecutor<Group>
