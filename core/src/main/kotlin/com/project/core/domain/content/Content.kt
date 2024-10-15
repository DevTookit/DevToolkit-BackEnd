package com.project.core.domain.content

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.section.Section
import com.project.core.internal.ContentType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Content(
    var name: String,
    @ManyToOne(fetch = FetchType.LAZY)
    var groupUser: GroupUser,
    @ManyToOne(fetch = FetchType.LAZY)
    var group: Group,
    @ManyToOne(fetch = FetchType.LAZY)
    val section: Section,
    @Enumerated(EnumType.STRING)
    val type: ContentType,
    @Column(columnDefinition = "LONGBLOB")
    var content: String?,
) : BaseEntity() {
    var codeDescription: String? = null
    var size: Long? = null
    var extension: String? = null
    var url: String? = null
    var visitCnt: Long = 0

    // 부모 폴더
    @ManyToOne(fetch = FetchType.LAZY)
    var parentFolder: Content? = null

    @OneToMany(mappedBy = "content", cascade = [(CascadeType.REMOVE)], orphanRemoval = true)
    var languages: MutableSet<ContentLanguage> = mutableSetOf()

    @OneToMany(mappedBy = "content", cascade = [(CascadeType.REMOVE)], orphanRemoval = true)
    var skills: MutableSet<ContentSkill> = mutableSetOf()

    @OneToMany(mappedBy = "content", cascade = [(CascadeType.REMOVE)], orphanRemoval = true)
    var attachments: MutableSet<ContentAttachment> = mutableSetOf()

    @OneToMany(mappedBy = "parentFolder", cascade = [(CascadeType.REMOVE)], orphanRemoval = true)
    val childrens: MutableSet<Content> = mutableSetOf()

    @OneToMany(mappedBy = "parentFolder", cascade = [(CascadeType.REMOVE)], orphanRemoval = true)
    var files: MutableSet<Content> = mutableSetOf()

    fun updateAttachments(attachments: List<ContentAttachment>) {
        this.attachments.clear()
        this.attachments.addAll(attachments)
    }

    fun updateSkills(skills: List<ContentSkill>) {
        this.skills.clear()
        this.skills.addAll(skills)
    }

    fun updateLanguages(languages: List<ContentLanguage>) {
        this.languages.clear()
        this.languages.addAll(languages)
    }
}
