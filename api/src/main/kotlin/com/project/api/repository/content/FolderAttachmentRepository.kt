package com.project.api.repository.content

import com.project.core.domain.content.Folder
import com.project.core.domain.content.FolderAttachment
import org.springframework.data.jpa.repository.JpaRepository

interface FolderAttachmentRepository : JpaRepository<FolderAttachment, Long> {
    fun findByFolder(folder: Folder): List<FolderAttachment>
}
