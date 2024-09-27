package com.project.api.repository.content

import com.project.core.domain.content.Content
import com.project.core.domain.content.ContentLanguage
import org.springframework.data.jpa.repository.JpaRepository

interface ContentLanguageRepository : JpaRepository<ContentLanguage, Long> {
    fun deleteByContent(content: Content)
}
