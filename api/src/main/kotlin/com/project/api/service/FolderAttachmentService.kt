package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.external.FileService
import com.project.api.internal.ErrorMessage
import com.project.api.internal.FilePath
import com.project.api.repository.content.FolderAttachmentRepository
import com.project.api.repository.content.FolderRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.FolderAttachmentCreateRequest
import com.project.api.web.dto.request.FolderAttachmentUpdateRequest
import com.project.api.web.dto.response.FolderAttachmentResponse
import com.project.api.web.dto.response.FolderAttachmentResponse.Companion.toResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.content.Folder
import com.project.core.domain.content.FolderAttachment
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class FolderAttachmentService(
    private val folderAttachmentRepository: FolderAttachmentRepository,
    private val folderRepository: FolderRepository,
    private val fileService: FileService,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
) {
    fun readOne(
        email: String,
        groupId: Long,
        folderAttachmentId: Long,
    ): FolderAttachmentResponse {
        validate(email, groupId)
        val attachment =
            folderAttachmentRepository
                .findByIdOrNull(folderAttachmentId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_FOLDER_FILE.message)
        return attachment.toResponse()
    }

    @Transactional
    fun create(
        email: String,
        request: FolderAttachmentCreateRequest,
        files: List<MultipartFile>,
    ): List<FolderAttachmentResponse> {
        validate(email, request.groupId)
        val parentFolder =
            folderRepository.findByIdOrNull(request.parentFolderId) ?: throw RestException.notFound(
                ErrorMessage.NOT_FOUND_FOLDER.message,
            )

        return files.map {
            val response = fileService.upload(it, FilePath.CONTENT.name)
            if (response.isSuccess) {
                folderAttachmentRepository
                    .save(
                        FolderAttachment(
                            folder = parentFolder,
                            name = it.name,
                            size = it.size,
                            extension = createExtension(it),
                            url = response.url!!,
                        ),
                    ).toResponse()
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
            folderAttachmentRepository.findByIdOrNull(request.folderAttachmentId) ?: throw RestException.notFound(
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
        folderAttachmentId: Long,
    ) {
        validate(email, groupId)
        folderAttachmentRepository.deleteById(folderAttachmentId)
    }

    @Transactional
    fun create(
        folder: Folder,
        files: List<MultipartFile>,
    ): List<FolderAttachmentResponse> =
        files.map { file ->
            val response = fileService.upload(file, FilePath.CONTENT.name)

            if (response.isSuccess) {
                folderAttachmentRepository
                    .save(
                        FolderAttachment(
                            folder = folder,
                            name = file.name,
                            size = file.size,
                            extension = createExtension(file),
                            url = response.url!!,
                        ),
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

        if (!groupUser.role.isActive()) throw RestException.authorized(ErrorMessage.UNAUTHORIZED.message)

        return UserValidateResponse(
            user = user,
            group = group,
            groupUser = groupUser,
        )
    }
}
