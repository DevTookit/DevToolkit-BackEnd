package com.project.api.repository.content

import com.project.core.domain.content.Content
import com.project.core.domain.group.GroupUser
import com.project.core.domain.section.Section
import com.project.core.internal.ContentType
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface ContentRepository : JpaRepository<Content, Long> {
    fun findByIdAndType(
        id: Long,
        type: ContentType,
    ): Content?

    fun findByIdAndTypeAndGroupUser(
        id: Long,
        type: ContentType,
        user: GroupUser,
    ): Content?

    @EntityGraph(attributePaths = ["groupUser"])
    fun findByIdAndSection(
        id: Long,
        section: Section,
    ): Content?
}
