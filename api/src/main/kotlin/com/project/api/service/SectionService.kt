package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.category.SectionRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.CategoryUpdateRequest
import com.project.api.web.dto.request.SectionCreateRequest
import com.project.api.web.dto.response.CategoryUpdateResponse
import com.project.api.web.dto.response.CategoryUpdateResponse.Companion.toCategoryUpdateResponse
import com.project.api.web.dto.response.SectionCreateResponse
import com.project.api.web.dto.response.SectionCreateResponse.Companion.toCategoryCreateResponse
import com.project.api.web.dto.response.SectionResponse
import com.project.api.web.dto.response.SectionResponse.Companion.toResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.section.Section
import com.project.core.internal.SectionType
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SectionService(
    private val sectionRepository: SectionRepository,
    private val userRepository: UserRepository,
    private val groupUserRepository: GroupUserRepository,
    private val sectionNotificationService: SectionNotificationService,
    private val groupRepository: GroupRepository,
) {
    fun readAll(
        email: String,
        pageable: Pageable,
        groupId: Long,
        parentSectionId: Long?,
    ): List<SectionResponse>? {
        val userResponse = validatePublic(email, groupId)
        if (userResponse.groupUser == null) {
            return parentSectionId?.let { id ->
                sectionRepository.findByIdAndPublic(id, true)?.let { parentSection ->
                    sectionRepository
                        .findByParentAndTypeIn(
                            section = parentSection,
                            types = listOf(SectionType.MENU, SectionType.REPOSITORY),
                            pageable = pageable,
                        ).map { it.toResponse() }
                }
            } ?: sectionRepository.findByGroupAndTypeAndPublicAndParentIsNull(userResponse.group, SectionType.MENU, true, pageable).map {
                it.toResponse()
            }
        }
        return parentSectionId?.let { id ->
            sectionRepository.findByIdOrNull(id)?.let { parentSection ->
                sectionRepository
                    .findByParentAndTypeIn(
                        section = parentSection,
                        types = listOf(SectionType.MENU, SectionType.REPOSITORY),
                        pageable = pageable,
                    ).map { it.toResponse() }
            }
        } ?: sectionRepository.findByGroupAndTypeAndParentIsNull(userResponse.group, SectionType.MENU, pageable).map {
            it.toResponse()
        }
    }

    @Transactional
    fun create(
        email: String,
        request: SectionCreateRequest,
    ): SectionCreateResponse {
        val userResponse = validate(email, request.groupId)

        if (!userResponse.groupUser!!.role.isTopAmin()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        val parent = request.parentSectionId?.let { sectionRepository.findByIdOrNull(it) }

        return sectionRepository
            .save(
                Section(
                    name = request.name,
                    group = userResponse.group,
                    isPublic = if (request.parentSectionId == null) request.isPublic!! else parent!!.isPublic,
                    type = request.type,
                    parent = if (request.parentSectionId != null) parent else null,
                ),
            ).also {
                if (request.type == SectionType.MENU && parent == null) sectionNotificationService.create(it, userResponse.groupUser)
            }.toCategoryCreateResponse(userResponse.group.id)
    }

    @Transactional
    fun update(
        email: String,
        request: CategoryUpdateRequest,
    ): CategoryUpdateResponse {
        val userResponse = validate(email, request.groupId)

        if (!userResponse.groupUser!!.role.isTopAmin()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        val section =
            sectionRepository.findByIdAndTypeIn(request.categoryId, listOf(SectionType.MENU, SectionType.REPOSITORY))
                ?: throw RestException.notFound(
                    ErrorMessage.NOT_FOUND_CATEGORY.message,
                )

        return section
            .apply {
                this.name = request.name ?: this.name
                this.isPublic = if (this.parent == null && request.isPublic != null) request.isPublic else this.isPublic
            }.toCategoryUpdateResponse(userResponse.group.id)
    }

    @Transactional
    fun delete(
        email: String,
        sectionId: Long,
    ) {
        val user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val section =
            sectionRepository.findByIdOrNull(sectionId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_CATEGORY.message,
            )

        val groupUser =
            groupUserRepository.findByUserAndGroup(user, section.group)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)

        if (!groupUser.role.isTopAmin()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        sectionRepository.delete(section)
    }

    private fun validate(
        email: String,
        groupId: Long,
    ): UserValidateResponse {
        val user =
            userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        val groupUser = (
            groupUserRepository.findByUserAndGroup(user, group)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)
        )

        if (!groupUser.role.isActive()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return UserValidateResponse(
            user = user,
            group = group,
            groupUser = groupUser,
        )
    }

    private fun validatePublic(
        email: String,
        groupId: Long,
    ): UserValidateResponse {
        val user =
            userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        val groupUser = groupUserRepository.findByUserAndGroup(user, group)

        if (group.isPublic) {
            return UserValidateResponse(
                user = user,
                group = group,
                groupUser = groupUser,
            )
        }

        if (!groupUser!!.role.isActive()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return UserValidateResponse(
            user = user,
            group = group,
            groupUser = groupUser,
        )
    }
}
