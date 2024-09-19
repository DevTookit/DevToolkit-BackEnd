package com.project.api.repository.notification

import com.project.core.domain.notification.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface NotificationRepository :
    JpaRepository<Notification, Long>,
    QuerydslPredicateExecutor<Notification>
