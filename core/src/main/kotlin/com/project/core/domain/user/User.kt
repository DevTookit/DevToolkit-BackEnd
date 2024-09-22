package com.project.core.domain.user

import com.project.core.domain.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    val email: String,
    var password: String,
    var name: String,
    var img: String?,
) : BaseEntity() {
    @OneToMany(mappedBy = "user", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var hashTags: MutableSet<UserHashTag> = mutableSetOf()
    var isVerified: Boolean = false

    var isEnabled = true
    var isOnBoardingComplete: Boolean = false

    var failCount = 0
        set(value) {
            if (value >= 5) {
                isEnabled = false
                field = 5
            } else {
                isEnabled = true
                field = value
            }
        }
}
