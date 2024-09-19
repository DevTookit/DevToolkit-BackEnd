package com.project.core.domain.category

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.Group
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Category(
    var name: String,
    var isPublic: Boolean = false,
    @ManyToOne(fetch = FetchType.LAZY) val group: Group,
) : BaseEntity() {
    @OneToMany(mappedBy = "category", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var categoryNotifications: MutableSet<CategoryNotification> = mutableSetOf()
}
