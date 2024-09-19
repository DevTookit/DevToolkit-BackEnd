package com.project.core.domain.category

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.GroupUser
import com.project.core.internal.CategoryNotificationType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class CategoryNotification(
    @ManyToOne(fetch = FetchType.LAZY) val category: Category,
    @ManyToOne(fetch = FetchType.LAZY) val groupUser: GroupUser,
    @Enumerated(value = EnumType.STRING)
    var type: CategoryNotificationType = CategoryNotificationType.ALL,
) : BaseEntity()
