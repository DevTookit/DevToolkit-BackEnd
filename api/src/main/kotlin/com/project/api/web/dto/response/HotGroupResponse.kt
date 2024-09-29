package com.project.api.web.dto.response

import com.project.core.domain.group.Group

data class HotGroupResponse(
    val groupId: Long?,
    val groupName: String,
    val groupDescription: String?,
    val groupImg: String?,
    val groupUserCnt: Long,
    // user id
    val groupCreator: Long?,
    val groupCreatorName: String,
    val groupCreatorImg: String?,
) {
    companion object {
        fun Group.toHotGroupResponse() =
            HotGroupResponse(
                groupId = this.id,
                groupName = this.name,
                groupDescription = this.description,
                groupImg = this.img,
                groupUserCnt = this.groupUsers.size.toLong(),
                groupCreator = this.user.id,
                groupCreatorName = this.user.name,
                groupCreatorImg = this.user.img,
            )
    }
}
