package com.project.api.web.dto.response

import com.project.core.domain.announcement.Announcement

data class AnnounceUpdateResponse(
    val announceId: Long?,
    val name: String,
    val content: String,
) {
    companion object {
        fun Announcement.toAnnounceUpdateResponse(): AnnounceUpdateResponse =
            AnnounceUpdateResponse(
                announceId = this.id,
                name = this.name,
                content = this.content,
            )
    }
}
