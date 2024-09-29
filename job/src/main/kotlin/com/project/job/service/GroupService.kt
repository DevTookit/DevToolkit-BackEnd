package com.project.job.service

import com.project.core.domain.statistics.HotGroup
import com.project.job.repository.GroupRepository
import com.project.job.repository.HotGroupRepository
import com.project.job.service.dto.HotGroupResponse.Companion.toHotGroupResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupService(
    private val redisService: RedisService,
    private val groupRepository: GroupRepository,
    private val hotGroupRepository: HotGroupRepository,
) {
    @Transactional
    fun updateVisit() {
        redisService.getList("VISIT_GROUP", Long::class.java)?.forEach {
            groupRepository.findByIdOrNull(it)?.let {
                it.visitCnt++
            }
        }
    }

    @Transactional
    fun updateHotGroup() {
        val groupIdList = redisService.getList("JOIN_GROUP", Long::class.java)
        val groupJoinCount = mutableMapOf<Long, Long>()
        groupIdList?.forEach { groupId ->
            groupJoinCount[groupId] = groupJoinCount.getOrDefault(groupId, 0) + 1
        }

        groupRepository.findAllByIsPublic(true).map { group ->
            hotGroupRepository.findByGroup(group)?.let {
                it.visitCnt += group.visitCnt
                it.userCnt = group.groupUsers.size.toLong()
                it.joinCnt = groupJoinCount[group.id] ?: 0

                it.toHotGroupResponse()
            } ?: run {
                hotGroupRepository
                    .save(
                        HotGroup(
                            group = group,
                            visitCnt = group.visitCnt,
                            userCnt = group.groupUsers.size.toLong(),
                            joinCnt = groupJoinCount[group.id] ?: 0,
                        ),
                    ).toHotGroupResponse()
            }
        }
    }

    @Transactional
    fun updateHotGroupCache() {
        val hotGroups =
            hotGroupRepository
                .findTopBySumOfCounts(PageRequest.of(0, 10))
                .map {
                    it.toHotGroupResponse()
                }

        redisService.add("HOT_GROUP", hotGroups, 14400)
    }
}
