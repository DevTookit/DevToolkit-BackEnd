package com.project.api.web.dto.response

import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.user.User

data class UserValidateResponse(
    val user: User,
    val groupUser: GroupUser,
    val group: Group,
)
