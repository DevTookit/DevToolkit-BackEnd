package com.project.api.web.dto.response

import com.project.core.domain.user.User

data class UserResponse(
    val id: Long?,
    val email: String,
    val name: String,
    val img: String?,
    val tags: List<String>,
    val job: String?,
) {
    companion object {
        fun User.toUserResponse() =
            UserResponse(
                id = id,
                email = this.email,
                name = this.name,
                img = this.img,
                tags = this.hashTags.map { it.content },
                job = this.job,
            )
    }
}
