package com.project.api.repository.content

import com.project.core.domain.content.Folder
import com.project.core.domain.group.Group
import com.project.core.domain.section.Section
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FolderRepository : JpaRepository<Folder, Long> {
    fun findByGroupAndSectionAndParentIsNull(
        group: Group,
        section: Section,
        pageable: Pageable,
    ): List<Folder>

    fun findByGroupAndSectionAndParent(
        group: Group,
        section: Section,
        parent: Folder,
        pageable: Pageable,
    ): List<Folder>
}
