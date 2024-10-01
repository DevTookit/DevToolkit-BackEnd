package com.project.core.domain.group

import com.project.core.domain.BaseEntity
import com.project.core.domain.content.Content
import com.project.core.domain.section.SectionNotification
import com.project.core.domain.user.User
import com.project.core.internal.GroupRole
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "group_users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["group_id", "name"])],
)
class GroupUser(
    @ManyToOne val user: User,
    @ManyToOne @JoinColumn(name = "group_id") val group: Group,
    @Enumerated(value = EnumType.STRING)
    var role: GroupRole = GroupRole.PENDING,
) : BaseEntity() {
    var name: String = user.name

    // 관리자 초대에 대한 accept 여부
    var isAccepted = false

    // 가입 요청에 대한 관리자 승인
    var isApproved = false
        set(value) {
            if (value) {
                field = true
                isAccepted = true
            }
        }

    @OneToMany(mappedBy = "groupUser", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var sectionNotifications: MutableSet<SectionNotification> = mutableSetOf()

    @OneToMany(mappedBy = "groupUser", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var contents: MutableSet<Content> = mutableSetOf()
}
