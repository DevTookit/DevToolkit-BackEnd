package com.project.api.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.project.api.commons.exception.RestException
import com.project.api.external.FileService
import com.project.api.internal.ErrorMessage
import com.project.api.internal.FilePath
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.GroupCreateRequest
import com.project.api.web.dto.request.GroupUpdateRequest
import com.project.api.web.dto.response.GroupResponse
import com.project.api.web.dto.response.GroupResponse.Companion.toGroupResponse
import com.project.api.web.dto.response.GroupResponse.Companion.toResponse
import com.project.api.web.dto.response.HotGroupResponse
import com.project.api.web.dto.response.HotGroupResponse.Companion.toHotGroupResponse
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.group.QGroup
import com.project.core.internal.GroupRole
import com.querydsl.core.BooleanBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val userRepository: UserRepository,
    private val fileService: FileService,
    private val redisService: RedisService,
    private val objectMapper: ObjectMapper,
) {
    fun readMine(
        email: String,
        pageable: Pageable,
    ): List<GroupResponse> {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)

        return groupRepository.findByUser(user, pageable).map {
            it.toResponse()
        }
    }

    fun readMe(
        email: String,
        pageable: Pageable,
    ): List<GroupResponse> {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        return groupUserRepository
            .findByUserAndRoleIn(user, listOf(GroupRole.USER, GroupRole.TOP_MANAGER, GroupRole.MANAGER))
            .map {
                it.toGroupResponse()
            }
    }

    fun readAll(
        name: String?,
        pageable: Pageable,
    ): Page<GroupResponse> =
        groupRepository
            .findAll(
                BooleanBuilder()
                    .and(
                        name?.let { QGroup.group.name.containsIgnoreCase(name) },
                    ),
                pageable,
            ).map {
                it.toResponse()
            }

    fun readOne(
        email: String,
        groupId: Long,
    ): GroupResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        if (group.isPublic) {
            redisService.addList("VISIT_GROUP", groupId)
            return group.toResponse()
        }

        val groupUser =
            groupUserRepository.findByUserAndGroup(user, group) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)
        if (!groupUser.role.isActive()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return group.toResponse()
    }

    @Transactional
    fun create(
        email: String,
        request: GroupCreateRequest,
        img: MultipartFile?,
    ): GroupResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository
                .save(
                    Group(
                        user = user,
                        name = request.name,
                        description = request.description,
                        img = img?.let { fileService.upload(it, FilePath.PROFILE.name).url },
                        isPublic = request.isPublic,
                    ),
                ).also {
                    groupUserRepository.save(
                        GroupUser(
                            user = user,
                            group = it,
                            role = GroupRole.TOP_MANAGER,
                        ).apply {
                            isAccepted = true
                            isApproved = true
                        },
                    )
                }

        return group.toResponse()
    }

    @Transactional
    fun update(
        email: String,
        request: GroupUpdateRequest,
        img: MultipartFile?,
    ): GroupResponse {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(request.id)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        if (group.user.id != user.id) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        return groupRepository
            .save(
                group.apply {
                    this.name = request.name
                    this.img = img?.let { fileService.upload(it, FilePath.PROFILE.name).url }
                    this.description = request.description
                    this.isPublic = request.isPublic
                },
            ).toResponse()
    }

    @Transactional
    fun delete(
        email: String,
        id: Long,
    ) {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(id)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        if (group.user.id != user.id) {
            throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
        }

        groupRepository.delete(group)
    }

    fun readHot(): List<HotGroupResponse> {
        val groupList = redisService.get("HOT_GROUP")

        return if (groupList != null) {
            val typeRef = object : TypeReference<List<HotGroupResponse>>() {}
            objectMapper.readValue(groupList.toString(), typeRef)
        } else {
            val list =
                groupRepository
                    .findAllByIsPublicIsTrueOrderByGroupUsersSizeDesc(PageRequest.of(0, 10))
                    .map {
                        it.toHotGroupResponse()
                    }
            redisService.add("HOT_GROUP", list, 14400)

            return list
        }
    }
}
