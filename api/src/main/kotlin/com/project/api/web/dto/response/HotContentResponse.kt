package com.project.api.web.dto.response

import com.project.core.domain.content.Content

data class HotContentResponse(
    val contentId: Long?,
    val groupId: Long?,
    val section: Long?,
    val writerId: Long?,
    val writerName: String,
    val writerImg: String? = null,
    val writerJob: String? = null,
    val contentName: String,
    val content: String? = null,
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
                writerJob = this.groupUser.user.job,
                contentName = this.name,
                content = this.content,
            )
    }
}
