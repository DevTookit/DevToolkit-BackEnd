package com.project.job.service.dto

import com.project.core.domain.content.Content

data class HotContentResponse(
    val contentId: Long?,
    val groupId: Long?,
    val section: Long?,
    val writerId: Long?,
    val writerName: String,
    val writerImg: String?,
    val contentName: String,
    val content: String?,
) {
    companion object {
        fun Content.toHotContentResponse(): HotContentResponse =
            HotContentResponse(
                contentId = this.id,
                groupId = this.group.id,
                section = this.section.id,
                writerId = this.groupUser.user.id,
                writerName = this.groupUser.name,
                writerImg = this.groupUser.user.img,
                contentName = this.groupUser.name,
                content = this.content,
            )
    }
}
