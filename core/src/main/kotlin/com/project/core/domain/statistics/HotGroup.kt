package com.project.core.domain.statistics

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.Group
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne

@Entity
class HotGroup(
    @OneToOne
    val group: Group,
    var visitCnt: Long,
    var userCnt: Long,
    var joinCnt: Long,
) : BaseEntity()
