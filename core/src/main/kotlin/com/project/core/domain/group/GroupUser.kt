package com.project.core.domain.group

import com.project.core.domain.BaseEntity
import com.project.core.domain.user.User
import com.project.core.internal.GroupRole
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "group_users")
class GroupUser(
    @ManyToOne val user: User,
    // 등급, 확동가능, 초대확인, 그룹아이디
    @ManyToOne val group: Group,
    @Enumerated(EnumType.STRING)
    var role: GroupRole = GroupRole.USER,
) : BaseEntity() {
    var isAccepted = false
        set(value) {
            if (value) {
                field = true
                isEnable = true
            }
        }

    var isEnable = false
}
