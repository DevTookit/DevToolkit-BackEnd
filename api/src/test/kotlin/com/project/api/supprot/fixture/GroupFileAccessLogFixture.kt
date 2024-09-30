package com.project.api.supprot.fixture

import com.project.api.repository.group.GroupFileAccessLogRepository
import com.project.core.domain.content.Content
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupFileAccessLog
import com.project.core.domain.user.User
import org.springframework.stereotype.Component

@Component
class GroupFileAccessLogFixture(
    private val groupFileAccessLogRepository: GroupFileAccessLogRepository,
) : Fixture {
    fun create(
        user: User,
        content: Content,
        group: Group,
        lastAccessAt: Long = System.currentTimeMillis(),
    ): GroupFileAccessLog =
        groupFileAccessLogRepository.save(
            GroupFileAccessLog(
                user = user,
                content = content,
                lastAccessAt = lastAccessAt,
                group = group,
            ),
        )

    override fun tearDown() {
        groupFileAccessLogRepository.deleteAll()
    }
}
