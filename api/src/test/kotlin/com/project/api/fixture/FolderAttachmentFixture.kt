package com.project.api.fixture

import com.project.api.repository.content.FolderAttachmentRepository
import com.project.core.domain.content.Folder
import com.project.core.domain.content.FolderAttachment
import org.springframework.stereotype.Component
import java.util.Random
import java.util.UUID

@Component
class FolderAttachmentFixture(
    private val folderAttachmentRepository: FolderAttachmentRepository,
) : Fixture {
    fun create(
        folder: Folder,
        name: String = UUID.randomUUID().toString(),
        size: Long = Random().nextLong(),
        extension: String = "pdf",
        url: String = UUID.randomUUID().toString(),
    ) = folderAttachmentRepository.save(
        FolderAttachment(
            folder = folder,
            name = name,
            size = size,
            extension = extension,
            url = url,
        ),
    )

    override fun tearDown() {
        folderAttachmentRepository.deleteAll()
    }
}
