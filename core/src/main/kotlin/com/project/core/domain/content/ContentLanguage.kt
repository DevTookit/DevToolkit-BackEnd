package com.project.core.domain.content

import com.project.core.domain.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class ContentLanguage(
    @ManyToOne(fetch = FetchType.LAZY)
    val content: Content,
    val name: String,
) : BaseEntity()
