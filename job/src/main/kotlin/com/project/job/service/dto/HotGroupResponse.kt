package com.project.job.service.dto

import com.project.core.domain.statistics.HotGroup

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
        fun HotGroup.toHotGroupResponse() =
            HotGroupResponse(
                groupId = this.group.id,
                groupName = this.group.name,
                groupDescription = this.group.description,
                groupImg = this.group.img,
                groupUserCnt =
                    this.group.groupUsers.size
                        .toLong(),
                groupCreator = this.group.user.id,
                groupCreatorName = this.group.user.name,
                groupCreatorImg = this.group.user.img,
            )
    }
}
