package com.project.api.web.dto.request

data class AnnounceUpdateRequest(
    val announceId: Long,
    val name: String?,
    val content: String?,
)
