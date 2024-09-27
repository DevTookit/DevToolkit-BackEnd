package com.project.api.repository.content

import com.project.api.web.dto.response.ContentSearchResponse
import com.project.core.domain.user.User
import com.project.core.internal.ContentType
import org.springframework.data.domain.Pageable

interface ContentRepositorySupport {
    fun search(
        user: User,
        name: String?,
        groupId: Long?,
        languages: List<String>?,
        skills: List<String>?,
        writer: String?,
        startDate: Long?,
        endDate: Long?,
        pageable: Pageable,
        type: ContentType?,
    ): List<ContentSearchResponse>
}
