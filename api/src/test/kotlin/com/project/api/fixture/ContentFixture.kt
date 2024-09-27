package com.project.api.fixture

import com.project.api.repository.content.ContentRepository
import com.project.core.domain.content.Content
import com.project.core.domain.content.ContentAttachment
import com.project.core.domain.content.ContentLanguage
import com.project.core.domain.content.ContentSkill
import com.project.core.domain.content.Folder
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.section.Section
import com.project.core.internal.ContentType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ContentFixture(
    private val contentRepository: ContentRepository,
) : Fixture {
    fun create(
        name: String = UUID.randomUUID().toString(),
        groupUser: GroupUser,
        group: Group,
        section: Section,
        type: ContentType = ContentType.BOARD,
        content: String = UUID.randomUUID().toString(),
        codeDescription: String = UUID.randomUUID().toString(),
        size: Long = 200,
        extension: String = "jpg",
        url: String = UUID.randomUUID().toString(),
        folder: Folder? = null,
        languages: List<ContentLanguage>? = null,
        skills: List<ContentSkill>? = null,
        attachments: List<ContentAttachment>? = null,
    ): Content =
        contentRepository.save(
            Content(
                name = name,
                group = group,
                groupUser = groupUser,
                section = section,
                type = type,
                content = content,
            ).apply {
                if (type == ContentType.FILE) {
                    this.extension = extension
                    this.size = size
                    this.url = url
                    attachments?.let {
                        this.attachments.addAll(it)
                    }
                } else if (type == ContentType.CODE) {
                    this.codeDescription = codeDescription
                }

                if (type == ContentType.BOARD || type == ContentType.CODE) {
                    languages?.let {
                        this.languages.addAll(it)
                    }
                    skills?.let {
                        this.skills.addAll(it)
                    }
                }
            },
        )

    override fun tearDown() {
        contentRepository.deleteAll()
    }
}
