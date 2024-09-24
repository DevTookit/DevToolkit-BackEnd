package com.project.api.fixture

import com.project.api.repository.content.FolderRepository
import com.project.core.domain.content.Folder
import com.project.core.domain.group.Group
import com.project.core.domain.section.Section
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class FolderFixture(
    private val folderRepository: FolderRepository,
) : Fixture {
    fun create(
        name: String = UUID.randomUUID().toString(),
        group: Group,
        section: Section,
        parent: Folder? = null,
    ) = folderRepository.save(
        Folder(
            name = name,
            group = group,
            section = section,
            parent = parent,
        ),
    )

    override fun tearDown() {
        folderRepository.deleteAll()
    }
}
