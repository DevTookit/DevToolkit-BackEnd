package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.internal.ErrorMessage
import com.project.api.repository.category.SectionRepository
import com.project.api.repository.content.FolderAttachmentRepository
import com.project.api.repository.content.FolderRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.FolderCreateRequest
import com.project.api.web.dto.request.FolderUpdateRequest
import com.project.api.web.dto.response.FolderAttachmentResponse.Companion.toResponse
import com.project.api.web.dto.response.FolderCreateResponse
import com.project.api.web.dto.response.FolderCreateResponse.Companion.toFolderCreateResponse
import com.project.api.web.dto.response.FolderResponse
import com.project.api.web.dto.response.FolderResponse.Companion.toResponse
import com.project.api.web.dto.response.FolderSubResponse.Companion.toFolderSubResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.content.Folder
import com.project.core.internal.SectionType
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class FolderContentService(
    private val folderRepository: FolderRepository,
    private val folderAttachmentRepository: FolderAttachmentRepository,
    private val sectionRepository: SectionRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val folderAttachmentService: FolderAttachmentService,
) {
    fun readAll(
        email: String,
        pageable: Pageable,
        groupId: Long,
        sectionId: Long,
        parentFolderId: Long?,
    ): FolderResponse {
        val userResponse = validate(email, groupId)
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
                        pageable = pageable,
                    ).map {
                        it.toFolderSubResponse()
                    }

            val subAttachments =
                folderAttachmentRepository
                    .findByFolder(parentFolder)
                    .map {
                        it.toResponse()
                    }

            parentFolder.toResponse(
                attachments = subAttachments,
                subFolders = subFolders,
            )
        } ?: run {
            val subFolders =
                folderRepository
                    .findByGroupAndSectionAndParentIsNull(
                        group = userResponse.group,
                        section = section,
                        pageable = pageable,
                    ).map {
                        it.toFolderSubResponse()
                    }

            FolderResponse(
                parentId = null,
                name = null,
                subFolders = subFolders,
            )
        }
    }

    @Transactional
    fun create(
        email: String,
        request: FolderCreateRequest,
        files: List<MultipartFile>?,
    ): FolderCreateResponse {
        val userResponse = validate(email, request.groupId)
        val section =
            sectionRepository.findByIdOrNull(
                request.sectionId,
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
                ),
            ).run {
                toFolderCreateResponse(
                    files?.let {
                        folderAttachmentService.create(this, it)
                    },
                )
            }
    }

    @Transactional
    fun update(
        email: String,
        request: FolderUpdateRequest,
    ): FolderResponse {
        validate(email, request.groupId)
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
}
