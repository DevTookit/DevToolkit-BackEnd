package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.external.FileService
import com.project.api.internal.ErrorMessage
import com.project.api.internal.FilePath
import com.project.api.repository.category.SectionRepository
import com.project.api.repository.content.ContentRepository
import com.project.api.repository.content.FolderRepository
import com.project.api.repository.group.GroupFileAccessLogRepository
import com.project.api.repository.group.GroupLogRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.FolderAttachmentCreateRequest
import com.project.api.web.dto.request.FolderAttachmentUpdateRequest
import com.project.api.web.dto.response.FolderAttachmentResponse
import com.project.api.web.dto.response.FolderAttachmentResponse.Companion.toResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.content.Content
import com.project.core.domain.content.Folder
import com.project.core.domain.group.GroupFileAccessLog
import com.project.core.domain.group.GroupLog
import com.project.core.domain.group.QGroup.group
import com.project.core.internal.ContentType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class FolderAttachmentService(
    private val contentRepository: ContentRepository,
    private val folderRepository: FolderRepository,
    private val fileService: FileService,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val groupLongRepository: GroupLogRepository,
    private val groupFileAccessLogRepository: GroupFileAccessLogRepository,
    private val sectionRepository: SectionRepository,
) {
    fun readOne(
        email: String,
        groupId: Long,
        sectionId: Long,
        folderAttachmentId: Long,
    ): FolderAttachmentResponse {
        val userResponse = validate(email, groupId)
        sectionRepository.findByIdOrNull(sectionId)?.let { section ->
            if (!section.isPublic && userResponse.groupUser == null) {
                throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)
            }
        }
        val attachment =
            contentRepository
                .findByIdAndType(folderAttachmentId, ContentType.FILE)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_FOLDER_FILE.message)

        groupFileAccessLogRepository
            .findByContentAndUser(
                attachment,
                userResponse.user,
            )?.let {
                it.lastAccessAt = System.currentTimeMillis()
            } ?: run {
            groupFileAccessLogRepository.save(
                GroupFileAccessLog(
                    user = userResponse.user,
                    content = attachment,
                    lastAccessAt = System.currentTimeMillis(),
                    group = userResponse.group,
                ),
            )
        }

        return attachment.toResponse()
    }

    @Transactional
    fun create(
        email: String,
        request: FolderAttachmentCreateRequest,
        files: List<MultipartFile>,
    ): List<FolderAttachmentResponse> {
        val userResponse = validate(email, request.groupId)
        val parentFolder =
            folderRepository.findByIdOrNull(request.parentFolderId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_FOLDER.message,
            )
        return files.map {
            val response = fileService.upload(it, FilePath.CONTENT.name)
            if (response.isSuccess) {
                contentRepository
                    .save(
                        Content(
                            name = it.name,
                            groupUser = userResponse.groupUser!!,
                            section = parentFolder.section,
                            type = ContentType.FILE,
                            content = null,
                            group = userResponse.group,
                        ).apply {
                            folder = parentFolder
                            size = it.size
                            extension = createExtension(it)
                            url = response.url
                        },
                    ).also { content ->
                        groupLongRepository.save(
                            GroupLog(
                                user = userResponse.user,
                                group = userResponse.group,
                                type = content.type,
                                contentId = content.id!!,
                                contentName = content.name,
                                sectionId = parentFolder.section.id!!,
                            ),
                        )
                    }.toResponse()
            } else {
                throw RestException.badRequest(message = response.errorMessage!!)
            }
        }
    }

    @Transactional
    fun update(
        email: String,
        request: FolderAttachmentUpdateRequest,
    ): FolderAttachmentResponse {
        validate(email, request.groupId)
        val folderAttachment =
            contentRepository.findByIdAndType(request.folderAttachmentId, ContentType.FILE) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_FOLDER_FILE.message,
            )
        return folderAttachment
            .apply {
                this.name = request.name
            }.toResponse()
    }

    @Transactional
    fun delete(
        email: String,
        groupId: Long,
        sectionId: Long,
        folderAttachmentId: Long,
    ) {
        validate(email, groupId)
        contentRepository.deleteById(folderAttachmentId)
    }

    @Transactional
    fun create(
        folder: Folder,
        files: List<MultipartFile>,
        userResponse: UserValidateResponse,
    ): List<FolderAttachmentResponse> =
        files.map { file ->
            val response = fileService.upload(file, FilePath.CONTENT.name)

            if (response.isSuccess) {
                contentRepository
                    .save(
                        Content(
                            name = file.originalFilename!!,
                            groupUser = userResponse.groupUser!!,
                            section = folder.section,
                            type = ContentType.FILE,
                            content = null,
                            group = userResponse.group,
                        ).apply {
                            this.folder = folder
                            size = file.size
                            extension = createExtension(file)
                            url = response.url
                        }.also {
                            groupLongRepository.save(
                                GroupLog(
                                    user = userResponse.user,
                                    group = userResponse.group,
                                    type = it.type,
                                    contentId = it.id!!,
                                    contentName = it.name,
                                    sectionId = folder.section.id!!,
                                ),
                            )
                        },
                    ).toResponse()
            } else {
                throw RestException.badRequest(message = response.errorMessage!!)
            }
        }

    private fun createExtension(file: MultipartFile) = file.originalFilename!!.substring(file.originalFilename!!.lastIndexOf(".") + 1)

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
