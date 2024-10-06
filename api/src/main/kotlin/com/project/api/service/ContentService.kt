package com.project.api.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.project.api.commons.exception.RestException
import com.project.api.external.FileService
import com.project.api.internal.ErrorMessage
import com.project.api.internal.FilePath
import com.project.api.internal.RedisType
import com.project.api.repository.bookmark.BookmarkRepository
import com.project.api.repository.category.SectionRepository
import com.project.api.repository.content.ContentAttachmentRepository
import com.project.api.repository.content.ContentLanguageRepository
import com.project.api.repository.content.ContentRepository
import com.project.api.repository.content.ContentSkillRepository
import com.project.api.repository.group.GroupLogRepository
import com.project.api.repository.group.GroupRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.user.UserRepository
import com.project.api.web.dto.request.ContentCreateRequest
import com.project.api.web.dto.request.ContentUpdateRequest
import com.project.api.web.dto.response.ContentCreateResponse
import com.project.api.web.dto.response.ContentCreateResponse.Companion.toContentCreateResponse
import com.project.api.web.dto.response.ContentResponse
import com.project.api.web.dto.response.ContentResponse.Companion.toResponse
import com.project.api.web.dto.response.ContentSearchResponse
import com.project.api.web.dto.response.ContentUpdateResponse
import com.project.api.web.dto.response.ContentUpdateResponse.Companion.toContentUpdateResponse
import com.project.api.web.dto.response.HotContentResponse
import com.project.api.web.dto.response.HotContentResponse.Companion.toHotContentResponse
import com.project.api.web.dto.response.UserValidateResponse
import com.project.core.domain.content.Content
import com.project.core.domain.content.ContentAttachment
import com.project.core.domain.content.ContentLanguage
import com.project.core.domain.content.ContentSkill
import com.project.core.domain.group.GroupLog
import com.project.core.domain.group.QGroup.group
import com.project.core.domain.user.User
import com.project.core.internal.ContentType
import com.project.core.internal.SectionType
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ContentService(
    private val contentRepository: ContentRepository,
    private val userRepository: UserRepository,
    private val redisService: RedisService,
    private val contentAttachmentRepository: ContentAttachmentRepository,
    private val contentSkillRepository: ContentSkillRepository,
    private val contentLanguageRepository: ContentLanguageRepository,
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val sectionRepository: SectionRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val fileService: FileService,
    private val groupLogRepository: GroupLogRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional(readOnly = true)
    fun readAll(
        email: String,
        groupId: Long?,
        sectionId: Long?,
        name: String?,
        languages: List<String>?,
        skills: List<String>?,
        writer: String?,
        startDate: Long?,
        endDate: Long?,
        pageable: Pageable,
        type: ContentType?,
    ): List<ContentSearchResponse> {
        var user: User

        if (groupId == null) {
            user = userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        } else {
            user = validatePublic(email, groupId).user
        }

        return contentRepository
            .search(
                user = user,
                groupId = groupId,
                sectionId = sectionId,
                name = name,
                languages =
                    languages?.map {
                        it.lowercase().replaceFirstChar { it.uppercase() }
                    },
                skills =
                    skills?.map {
                        it.lowercase().replaceFirstChar { it.uppercase() }
                    },
                writer = writer,
                startDate = startDate,
                endDate = endDate,
                pageable = pageable,
                type = type,
            ).map {
                val isBookmark = bookmarkRepository.existsByContentIdAndUser(it.contentId!!, user)
                it.isBookmark = isBookmark
                return@map it
            }
    }

    @Transactional(readOnly = true)
    fun read(
        email: String,
        groupId: Long,
        sectionId: Long,
        contentId: Long,
    ): ContentResponse {
        val userResponse = validatePublic(email, groupId)

        val section =
            sectionRepository.findByIdAndType(sectionId, SectionType.REPOSITORY)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_SECTION.message)

        val content = (
            contentRepository.findByIdAndSection(contentId, section)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_CONTENT.message)
        )

        increaseVisitCnt(contentId)

        return content
            .toResponse()
            .apply {
                val isBookmark = bookmarkRepository.existsByContentIdAndUser(this.contentId!!, userResponse.user)
                this.isBookmark = isBookmark
            }
    }

    private fun increaseVisitCnt(contentId: Long) {
        redisService.addList(RedisType.VISIT_CONTENT.name, contentId)
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
                    name = request.name,
                    groupUser = userResponse.groupUser!!,
                    section = section,
                    type = request.type,
                    content = request.content,
                    group = userResponse.group,
                ).apply {
                    if (request.type == ContentType.CODE) {
                        this.codeDescription = request.codeDescription
                    }
                }.also {
                    createContentExtraInfo(request, it)
                    createContentAttachment(request, files, it)
                },
            ).also {
                groupLogRepository.save(
                    GroupLog(
                        user = userResponse.user,
                        group = userResponse.group,
                        type = request.type,
                        contentId = it.id!!,
                        contentName = it.name,
                        sectionId = section.id!!,
                    ),
                )
            }.toContentCreateResponse()
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
        sectionRepository.findByIdAndType(sectionId, SectionType.REPOSITORY)
            ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_SECTION.message)

        val content =
            contentRepository.findByIdAndTypeAndGroupUser(request.contentId, request.type, userResponse.groupUser!!)
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
        validate(email, groupId)
        sectionRepository.findByIdAndType(sectionId, SectionType.REPOSITORY)
            ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_SECTION.message)

        contentRepository.deleteById(contentId)
    }

    fun readHot(): List<HotContentResponse> {
        val contentList = redisService.get(RedisType.HOT_CONTENT.name)

        return if (contentList != null) {
            val typeRef = object : TypeReference<List<HotContentResponse>>() {}
            objectMapper.readValue(contentList.toString(), typeRef)
        } else {
            val list =
                contentRepository
                    .findAllBySectionIsPublicTrueOrderByVisitCntDesc(PageRequest.of(0, 10))
                    .map {
                        it.toHotContentResponse()
                    }
            redisService.add("HOT_CONTENT", list, 14400)

            return list
        }
    }

    private fun Content.updateContent(
        request: ContentUpdateRequest,
        files: List<MultipartFile>?,
    ) {
        request.name?.let {
            this.name = it
        }
        request.content?.let {
            this.content = it
        }
        if (type == ContentType.CODE) {
            request.codeDescription?.let {
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
        request.languages?.forEach { language ->
            contentLanguageRepository.save(
                ContentLanguage(
                    content = content,
                    name = setContentInfo(language),
                ),
            )
        }

        request.skills?.forEach { skill ->
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

    private fun validatePublic(
        email: String,
        groupId: Long,
    ): UserValidateResponse {
        val user =
            userRepository.findByEmail(email) ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_USER.message)
        val group =
            groupRepository.findByIdOrNull(groupId)
                ?: throw RestException.notFound(ErrorMessage.NOT_FOUND_GROUP.message)

        val groupUser =
            groupUserRepository.findByUserAndGroup(user, group)

        if (group.isPublic) {
            UserValidateResponse(
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
