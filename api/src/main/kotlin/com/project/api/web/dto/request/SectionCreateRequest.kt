package com.project.api.web.dto.request

import com.project.core.internal.SectionType

data class SectionCreateRequest(
    val groupId: Long,
    val name: String,
    val isPublic: Boolean?,
    val parentSectionId: Long?,
    val type: SectionType,
)
