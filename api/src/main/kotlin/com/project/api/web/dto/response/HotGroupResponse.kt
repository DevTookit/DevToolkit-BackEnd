package com.project.api.web.dto.response

import com.project.api.web.dto.response.HotGroupResponse.HotGroupUserImg.Companion.toHotGroupUserImg
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser

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
    val groupUserImgs: List<HotGroupUserImg>? = null,
) {
    companion object {
        fun Group.toHotGroupResponse(groupUsers: List<GroupUser>) =
            HotGroupResponse(
                groupId = this.id,
                groupName = this.name,
                groupDescription = this.description,
                groupImg = this.img,
                groupUserCnt = this.groupUsers.size.toLong(),
                groupCreator = this.user.id,
                groupCreatorName = this.user.name,
                groupCreatorImg = this.user.img,
                groupUserImgs =
                    groupUsers.map {
                        it.toHotGroupUserImg()
                    },
            )
    }

    class HotGroupUserImg(
        val img: String?,
        val groupUserId: Long?,
    ) {
        companion object {
            fun GroupUser.toHotGroupUserImg() =
                HotGroupUserImg(
                    img = this.user.img,
                    groupUserId = this.id,
                )
        }
    }
}
