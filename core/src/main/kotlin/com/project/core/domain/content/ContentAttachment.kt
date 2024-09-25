package com.project.core.domain.content

import com.project.core.domain.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class ContentAttachment(
    @ManyToOne(fetch = FetchType.LAZY)
    val content: Content,
    var name: String,
    val size: Long,
    val extension: String,
    val url: String,
) : BaseEntity()
