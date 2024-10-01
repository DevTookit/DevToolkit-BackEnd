package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.internal.SortType
import com.project.api.repository.category.SectionRepository
import com.project.api.repository.content.ContentRepository
import com.project.api.repository.content.FolderRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.FolderCreateRequest
import com.project.api.web.dto.request.FolderUpdateRequest
import com.project.api.web.dto.response.FolderAttachmentResponse.Companion.toResponse
import com.project.api.web.dto.response.FolderCreateResponse
import com.project.api.web.dto.response.FolderCreateResponse.Companion.toFolderCreateResponse
import com.project.api.web.dto.response.FolderReadResponse
import com.project.api.web.dto.response.FolderReadResponse.Companion.toFolderReadResponse
import com.project.api.web.dto.response.FolderResponse
import com.project.api.web.dto.response.FolderResponse.Companion.toResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.content.Folder
import com.project.core.internal.ContentType
import com.project.core.internal.SectionType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class FolderContentService(
    private val folderRepository: FolderRepository,
    private val contentRepository: ContentRepository,
    private val sectionRepository: SectionRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val folderAttachmentService: FolderAttachmentService,
) {
    fun readAll(
        email: String,
        sortType: SortType,
        groupId: Long,
        sectionId: Long,
        parentFolderId: Long?,
    ): FolderResponse {
        val userResponse = validatePublic(email, groupId)
        val section =
            sectionRepository.findByIdAndType(sectionId, SectionType.REPOSITORY)
                ?: throw RestException.badRequest(ErrorMessage.NOT_FOUND_SECTION.message)

        if (section.type == SectionType.MENU) {
            throw RestException.badRequest(ErrorMessage.IMPOSSIBLE_CREATE_CONTENT.message)
        }

        // parentFolderId가 null이면 제일 상위 폴더이다.
        return parentFolderId?.let {
            val parentFolder =
                folderRepository.findByIdOrNull(it)
                    ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_FOLDER.message)

            val subFolders =
                folderRepository
                    .findByGroupAndSectionAndParent(
                        group = userResponse.group,
                        section = section,
                        parent = parentFolder,
                    ).map {
                        it.toFolderReadResponse()
                    }

            val subAttachments =
                contentRepository
                    .findByFolderAndType(parentFolder, ContentType.FILE)
                    .map {
                        it.toFolderReadResponse()
                    }

            val combinedList = (subFolders + subAttachments).sortedWith(getSortComparator(sortType))

            // parentFolder의 응답 생성
            parentFolder.toResponse(
                lists = combinedList,
            )
        } ?: run {
            val lists =
                folderRepository
                    .findByGroupAndSectionAndParentIsNull(
                        group = userResponse.group,
                        section = section,
                    ).map {
                        it.toFolderReadResponse()
                    }

            FolderResponse(
                parentId = null,
                name = null,
                lists = lists.sortedWith(getSortComparator(sortType)),
            )
        }
    }

    fun getSortComparator(sortType: SortType): Comparator<FolderReadResponse> =
        when (sortType) {
            SortType.NEW ->
                Comparator { o1, o2 ->
                    compareValuesBy(o1, o2, { it.lastModifiedDate })
                }
            SortType.NAME ->
                Comparator { o1, o2 ->
                    compareValuesBy(o1, o2, { it.name })
                }
            SortType.DEFAULT ->
                Comparator { o1, o2 ->
                    compareValuesBy(o1, o2, { it.id })
                }
        }

    @Transactional
    fun create(
        email: String,
        request: FolderCreateRequest,
        groupId: Long,
        sectionId: Long,
        files: List<MultipartFile>?,
    ): FolderCreateResponse {
        val userResponse = validate(email, groupId)
        val section =
            sectionRepository.findByIdOrNull(
                sectionId,
            ) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND.message)

        if (section.type != SectionType.REPOSITORY) {
            throw RestException.badRequest(ErrorMessage.IMPOSSIBLE_CREATE_CONTENT.message)
        }

        return folderRepository
            .save(
                Folder(
                    name = request.name,
                    group = userResponse.group,
                    section = section,
                    parent = request.parentFolderId?.let { folderRepository.findByIdOrNull(it) },
                    groupUser = userResponse.groupUser!!,
                ),
            ).run {
                toFolderCreateResponse(
                    files?.let {
                        folderAttachmentService.create(this, it, userResponse)
                    },
                )
            }
    }

    @Transactional
    fun update(
        email: String,
        groupId: Long,
        sectionId: Long,
        request: FolderUpdateRequest,
    ): FolderResponse {
        validate(email, groupId)
        val folder =
            folderRepository.findByIdOrNull(request.folderId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND.message)

        return folder
            .apply {
                this.name = request.name
            }.toResponse()
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

        val groupUser = (
            groupUserRepository.findByUserAndGroup(user, group)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP_USER.message)
        )
        if (group.isPublic) {
            return UserValidateResponse(
                user = user,
                group = group,
                groupUser = groupUser,
            )
        }

        if (!groupUser.role.isActive()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return UserValidateResponse(
            user = user,
            group = group,
            groupUser = groupUser,
        )
    }
}
