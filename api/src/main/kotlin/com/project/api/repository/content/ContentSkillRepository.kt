package com.project.api.repository.content

import com.project.core.domain.content.Content
import com.project.core.domain.content.ContentSkill
import org.springframework.data.jpa.repository.JpaRepository

interface ContentSkillRepository : JpaRepository<ContentSkill, Long> {
    fun deleteByContent(content: Content)
}
