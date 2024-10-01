package com.project.api.repository.category

import com.project.core.domain.group.Group
import com.project.core.domain.section.Section
import com.project.core.internal.SectionType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SectionRepository : JpaRepository<Section, Long> {
    fun findByGroupAndTypeAndParentIsNull(
        group: Group,
        type: SectionType,
        pageable: Pageable,
    ): List<Section>

    fun findByGroupAndTypeAndIsPublicAndParentIsNull(
        group: Group,
        type: SectionType,
        public: Boolean,
        pageable: Pageable,
    ): List<Section>

    fun findByGroupAndParentIsNull(group: Group): List<Section>

    fun findByIdAndType(
        id: Long,
        type: SectionType,
    ): Section?

    fun findByIdAndTypeIn(
        id: Long,
        types: List<SectionType>,
    ): Section?

    fun findByIdAndParentIsNull(id: Long): Section?

    fun findByParentAndTypeIn(
        section: Section,
        types: List<SectionType>,
        pageable: Pageable,
    ): List<Section>

    fun findByParentAndType(
        section: Section,
        type: SectionType,
        pageable: Pageable,
    ): List<Section>

    fun findByIdAndIsPublic(
        id: Long,
        public: Boolean,
    ): Section?
}
