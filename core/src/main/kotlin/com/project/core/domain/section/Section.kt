package com.project.core.domain.section

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.Group
import com.project.core.internal.SectionType
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Section(
    var name: String,
    var isPublic: Boolean = false,
    @ManyToOne(fetch = FetchType.LAZY) val group: Group,
    @Enumerated(EnumType.STRING) var type: SectionType,
    // 자식 폴더 기준으로
    @ManyToOne(fetch = FetchType.LAZY) var parent: Section? = null,
) : BaseEntity() {
    @OneToMany(mappedBy = "section", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var sectionNotifications: MutableSet<SectionNotification> = mutableSetOf()

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var childrens: MutableSet<Section> = mutableSetOf()
}
