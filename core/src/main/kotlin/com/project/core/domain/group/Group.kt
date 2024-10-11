package com.project.core.domain.group

import com.project.core.domain.BaseEntity
import com.project.core.domain.bookmark.Bookmark
import com.project.core.domain.section.Section
import com.project.core.domain.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "`groups`")
class Group(
    @ManyToOne(fetch = FetchType.LAZY) val user: User,
    @Column(unique = true)
    var name: String,
    var img: String?,
    var description: String?,
    var isPublic: Boolean,
) : BaseEntity() {
    var visitCnt: Long = 0

    @OneToMany(mappedBy = "group", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var groupUsers: MutableSet<GroupUser> = mutableSetOf()

    @OneToMany(mappedBy = "group", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var groupFileAccessLogs: MutableSet<GroupFileAccessLog> = mutableSetOf()

    @OneToMany(mappedBy = "group", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var sections: MutableSet<Section> = mutableSetOf()

    @OneToMany(mappedBy = "group", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var bookmarks: MutableSet<Bookmark> = mutableSetOf()

    @OneToMany(mappedBy = "group", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var groupLogs: MutableSet<GroupLog> = mutableSetOf()
}
