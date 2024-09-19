package com.project.api.repository.category

import com.project.core.domain.category.Category
import com.project.core.domain.group.Group
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByGroup(group: Group): List<Category>
}
