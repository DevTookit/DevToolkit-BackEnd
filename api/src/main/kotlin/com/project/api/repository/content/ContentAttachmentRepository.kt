package com.project.api.repository.content

import com.project.core.domain.content.Content
import com.project.core.domain.content.ContentAttachment
import org.springframework.data.jpa.repository.JpaRepository

interface ContentAttachmentRepository : JpaRepository<ContentAttachment, Long> {
    fun deleteByContent(content: Content)
}
