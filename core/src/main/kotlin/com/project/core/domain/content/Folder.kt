package com.project.core.domain.content

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.Group
import com.project.core.domain.section.Section
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Folder(
    var name: String,
    @ManyToOne(fetch = FetchType.LAZY)
    val group: Group,
    @ManyToOne(fetch = FetchType.LAZY)
    val section: Section,
    @ManyToOne(fetch = FetchType.LAZY)
    val parent: Folder? = null,
) : BaseEntity() {
    @OneToMany(mappedBy = "parent", cascade = [(CascadeType.REMOVE)], orphanRemoval = true)
    val children: MutableSet<Folder> = mutableSetOf()

    @OneToMany(mappedBy = "folder", cascade = [(CascadeType.REMOVE)], orphanRemoval = true)
    val attachments: MutableSet<Content> = mutableSetOf()
}
