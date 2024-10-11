package com.project.api.web.dto.response

import com.project.core.domain.announcement.Announcement

data class AnnounceCreateResponse(
    val announceId: Long?,
    val name: String,
    val content: String,
) {
    companion object {
        fun Announcement.toAnnounceCreateResponse(): AnnounceCreateResponse =
            AnnounceCreateResponse(
                announceId = this.id,
                name = this.name,
                content = this.content,
            )
    }
}
