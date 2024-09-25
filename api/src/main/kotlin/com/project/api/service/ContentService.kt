package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.external.FileService
import com.project.api.internal.ErrorMessage
import com.project.api.internal.FilePath
import com.project.api.repository.category.SectionRepository
import com.project.api.repository.content.ContentAttachmentRepository
import com.project.api.repository.content.ContentLanguageRepository
import com.project.api.repository.content.ContentRepository
import com.project.api.repository.content.ContentSkillRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.ContentCreateRequest
import com.project.api.web.dto.request.ContentUpdateRequest
import com.project.api.web.dto.response.ContentCreateResponse
import com.project.api.web.dto.response.ContentCreateResponse.Companion.toContentCreateResponse
import com.project.api.web.dto.response.ContentResponse
import com.project.api.web.dto.response.ContentResponse.Companion.toResponse
import com.project.api.web.dto.response.ContentUpdateResponse
import com.project.api.web.dto.response.ContentUpdateResponse.Companion.toContentUpdateResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.content.Content
import com.project.core.domain.content.ContentAttachment
import com.project.core.domain.content.ContentLanguage
import com.project.core.domain.content.ContentSkill
import com.project.core.internal.ContentType
import com.project.core.internal.SectionType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ContentService(
    private val contentRepository: ContentRepository,
    private val userRepository: UserRepository,
    private val contentAttachmentRepository: ContentAttachmentRepository,
    private val contentSkillRepository: ContentSkillRepository,
    private val contentLanguageRepository: ContentLanguageRepository,
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val sectionRepository: SectionRepository,
    private val fileService: FileService,
) {
    fun read(
        email: String,
        groupId: Long,
        sectionId: Long,
        contentId: Long,
    ): ContentResponse {
        val userResponse = validate(email, groupId)
        val section =
            sectionRepository.findByIdAndType(sectionId, SectionType.REPOSITORY)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_SECTION.message)

        val content = (
            contentRepository.findByIdAndSection(contentId, section)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_CONTENT.message)
        )

        return content.toResponse()
    }

    @Transactional
    fun create(
        email: String,
        groupId: Long,
        sectionId: Long,
        request: ContentCreateRequest,
        files: List<MultipartFile>?,
    ): ContentCreateResponse {
        val userResponse = validate(email, groupId)
        val section =
            sectionRepository.findByIdAndType(sectionId, SectionType.REPOSITORY)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_SECTION.message)

        return contentRepository
            .save(
                Content(
                    title = request.name,
                    groupUser = userResponse.groupUser,
                    section = section,
                    type = request.type,
                    content = request.content,
                ).apply {
                    if (request.type == ContentType.CODE) {
                        this.codeDescription = request.codeDescription
                    }
                }.also {
                    createContentExtraInfo(request, it)
                    createContentAttachment(request, files, it)
                },
            ).toContentCreateResponse()
    }

    @Transactional
    fun update(
        email: String,
        groupId: Long,
        sectionId: Long,
        request: ContentUpdateRequest,
        files: List<MultipartFile>?,
    ): ContentUpdateResponse {
        val userResponse = validate(email, groupId)
        val section =
            sectionRepository.findByIdAndType(sectionId, SectionType.REPOSITORY)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_SECTION.message)

        val content =
            contentRepository.findByIdAndTypeAndGroupUser(request.contentId, request.type, userResponse.groupUser)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_CONTENT.message)

        return content
            .apply {
                updateContent(request, files)
            }.toContentUpdateResponse()
    }

    @Transactional
    fun delete(
        email: String,
        groupId: Long,
        sectionId: Long,
        contentId: Long,
    ) {
        val userResponse = validate(email, groupId)
        val section =
            sectionRepository.findByIdAndType(sectionId, SectionType.REPOSITORY)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_SECTION.message)

        contentRepository.deleteById(contentId)
    }

    private fun Content.updateContent(
        request: ContentUpdateRequest,
        files: List<MultipartFile>?,
    ) {
        request.name?.let {
            this.title = it
        }
        request.content?.let {
            this.content = it
        }
        if (type == ContentType.CODE) {
            request.description?.let {
                this.codeDescription = it
            }
        }
        request.languages?.let {
            contentLanguageRepository.deleteByContent(this)
            val newLanguages =
                it.map {
                    contentLanguageRepository.save(
                        ContentLanguage(
                            content = this,
                            name = it,
                        ),
                    )
                }
            this.updateLanguages(newLanguages)
        }

        request.skills?.let {
            contentSkillRepository.deleteByContent(this)
            val newSkills =
                it.map {
                    contentSkillRepository.save(
                        ContentSkill(
                            content = this,
                            name = it,
                        ),
                    )
                }
            this.updateSkills(newSkills)
        }

        files?.let {
            this.attachments.forEach {
                fileService.delete(it.url)
                contentAttachmentRepository.delete(it)
            }
            val newAttachments =
                it.map {
                    uploadFile(it, this)
                }
            this.updateAttachments(newAttachments)
        }
    }

    private fun createContentAttachment(
        request: ContentCreateRequest,
        files: List<MultipartFile>?,
        content: Content,
    ) {
        if (request.type == ContentType.BOARD) {
            files?.map {
                uploadFile(it, content)
            }
        }
    }

    private fun uploadFile(
        file: MultipartFile,
        content: Content,
    ): ContentAttachment {
        val response = fileService.upload(file, FilePath.CONTENT.name)
        return if (response.isSuccess) {
            contentAttachmentRepository
                .save(
                    ContentAttachment(
                        content = content,
                        name = file.originalFilename!!,
                        size = file.size,
                        extension = createExtension(file),
                        url = response.url!!,
                    ),
                )
        } else {
            throw RestException.badRequest(message = response.errorMessage!!)
        }
    }

    private fun createContentExtraInfo(
        request: ContentCreateRequest,
        content: Content,
    ) {
        request.languages.forEach { language ->
            contentLanguageRepository.save(
                ContentLanguage(
                    content = content,
                    name = setContentInfo(language),
                ),
            )
        }

        request.skills.forEach { skill ->
            contentSkillRepository.save(
                ContentSkill(
                    content = content,
                    name = setContentInfo(skill),
                ),
            )
        }
    }

    private fun createExtension(file: MultipartFile) = file.originalFilename!!.substring(file.originalFilename!!.lastIndexOf(".") + 1)

    private fun setContentInfo(info: String): String = info.lowercase().replaceFirstChar { it.uppercase() }

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
