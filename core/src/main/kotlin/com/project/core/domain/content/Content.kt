package com.project.core.domain.content

import com.project.core.domain.BaseEntity
import com.project.core.domain.group.GroupUser
import com.project.core.domain.section.Section
import com.project.core.internal.ContentType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class Content(
    var title: String,
    @ManyToOne(fetch = FetchType.LAZY)
    var groupUser: GroupUser,
    @ManyToOne(fetch = FetchType.LAZY)
    val section: Section,
    @Enumerated(EnumType.STRING)
    val type: ContentType,
) : BaseEntity() {
    // content -> 코드설명, 컨텐츠 내용, 첨부파일(파일만일때)
    var content: String? = null
    var code: String? = null

    // type -> file일때
    val fileName: String? = null
    val fileSize: Long? = null
    val fileExtension: String? = null
}
