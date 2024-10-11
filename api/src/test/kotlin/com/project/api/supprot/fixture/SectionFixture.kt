package com.project.api.supprot.fixture

import com.project.api.repository.category.SectionNotificationRepository
import com.project.api.repository.category.SectionRepository
import com.project.core.domain.group.Group
import com.project.core.domain.section.Section
import com.project.core.internal.SectionType
import org.springframework.stereotype.Component
import java.util.Random
import java.util.UUID

@Component
class SectionFixture(
    private val sectionRepository: SectionRepository,
    private val sectionNotificationRepository: SectionNotificationRepository,
) : Fixture {
    fun create(
        name: String = UUID.randomUUID().toString(),
        isPublic: Boolean = Random().nextBoolean(),
        group: Group,
        type: SectionType = SectionType.MENU,
        parent: Section? = null,
    ) = sectionRepository.save(
        Section(name, isPublic, group, type)
            .apply {
                parent?.let {
                    this.parent = it
                }
                this.isPublic = isPublic
            },
    )

    override fun tearDown() {
        sectionNotificationRepository.deleteAll()
        sectionRepository.deleteAll()
    }
}
