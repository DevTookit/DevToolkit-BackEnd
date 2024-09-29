package com.project.job.service

import com.project.core.internal.ContentType
import com.project.job.repository.ContentRepository
import com.project.job.service.dto.HotContentResponse.Companion.toHotContentResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ContentService(
    private val contentRepository: ContentRepository,
    private val redisService: RedisService,
) {
    @Transactional
    fun updateVisit() {
        redisService.getList("VISIT_CONTENT", Long::class.java)?.forEach {
            contentRepository.findByIdOrNull(it)?.let {
                it.visitCnt++
            }
        }
    }

    fun updateHotContentCache() {
        val hotContentList =
            contentRepository
                .findAllByGroupIsPublicTrueAndTypeOrderByVisitCntDesc(ContentType.BOARD, PageRequest.of(0, 10))
                .map {
                    it.toHotContentResponse()
                }

        redisService.add("HOT_CONTENT", hotContentList, 14400)
    }
}
