package com.project.job.repository

import com.project.core.domain.content.Content
import com.project.core.internal.ContentType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ContentRepository : JpaRepository<Content, Long> {
    fun findAllBySectionIsPublicTrueAndTypeOrderByVisitCntDesc(
        type: ContentType,
        pageable: Pageable,
    ): List<Content>
}
