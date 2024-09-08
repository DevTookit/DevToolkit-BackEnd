package com.project.core.domain.user

import com.project.core.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.locationtech.jts.geom.Point

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    val email: String,
    var password: String,
    var name: String,
    var img: String?,
    var phoneNumber: String,
    var description: String,
    var point: Point,
) : BaseEntity() {
    var enabled = false
    var failCount = 0
        set(value) {
            if (value >= 5) {
                enabled = false
                field = 5
            } else {
                enabled = true
                field = value
            }
        }
}
