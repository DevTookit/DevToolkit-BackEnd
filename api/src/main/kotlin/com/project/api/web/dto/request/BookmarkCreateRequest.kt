package com.project.api.web.dto.request

import com.project.core.internal.BookmarkType

data class BookmarkCreateRequest(
    val groupId: Long,
    val type: BookmarkType,
    val contentId: Long,
)
