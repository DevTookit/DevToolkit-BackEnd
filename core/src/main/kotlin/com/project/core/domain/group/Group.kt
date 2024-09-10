package com.project.core.domain.group

import com.project.core.domain.BaseEntity
import com.project.core.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "`groups`")
class Group(
    @ManyToOne(fetch = FetchType.LAZY) val user: User,
    @Column(unique = true)
    var name: String,
    var img: String,
    var description: String,
) : BaseEntity()
// 주최자, 에름 사진, 그룹설명
