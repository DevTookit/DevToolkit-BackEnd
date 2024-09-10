package com.project.core.domain.user

import com.project.core.domain.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "users_hash_tag")
class UserHashTag(
    val content: String,
    @ManyToOne(fetch = FetchType.LAZY) val user: User,
) : BaseEntity()
