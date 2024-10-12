package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.repository.bookmark.BookmarkRepository
import com.project.api.repository.content.ContentRepository
import com.project.api.repository.group.GroupLogRepository
import com.project.api.supprot.fixture.BookmarkFixture
import com.project.api.supprot.fixture.ContentFixture
import com.project.api.supprot.fixture.GroupFixture
import com.project.api.supprot.fixture.GroupUserFixture
import com.project.api.supprot.fixture.SectionFixture
import com.project.api.supprot.fixture.UserFixture
import com.project.api.web.dto.request.ContentCreateRequest
import com.project.api.web.dto.request.ContentFileCreateRequest
import com.project.api.web.dto.request.ContentUpdateRequest
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.user.User
import com.project.core.internal.BookmarkType
import com.project.core.internal.ContentType
import com.project.core.internal.GroupRole
import com.project.core.internal.SectionType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.mock.web.MockMultipartFile

class ContentServiceTest(
    @Autowired private val contentService: ContentService,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val sectionFixture: SectionFixture,
    @Autowired private val contentFixture: ContentFixture,
    @Autowired private val groupLogRepository: GroupLogRepository,
    @Autowired private val bookmarkFixture: BookmarkFixture,
    @Autowired private val contentRepository: ContentRepository,
    @Autowired private val bookmarkRepository: BookmarkRepository,
) : TestCommonSetting() {
    lateinit var user: User
    lateinit var group: Group
    lateinit var groupUser: GroupUser

    @BeforeEach
    fun setup() {
        user = userFixture.create()
        group = groupFixture.create(user = user, isPublic = true)
        groupUser = groupUserFixture.create(group = group, user = userFixture.create())
    }

    @AfterEach
    fun tearDown() {
        bookmarkFixture.tearDown()
        contentFixture.tearDown()
        sectionFixture.tearDown()
        groupUserFixture.tearDown()
        groupLogRepository.deleteAll()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun readAll() {
        val group2 = groupFixture.create(user = user, isPublic = true)
        val groupUser2 = groupUserFixture.create(group = group2, user = userFixture.create())
        val section = sectionFixture.create(group = group)
        val section2 = sectionFixture.create(group = group2)
        val content =
            contentFixture.create(section = section, group = group, groupUser = groupUser, type = ContentType.CODE)
        contentFixture.create(section = section, group = group, groupUser = groupUser, type = ContentType.BOARD)
        contentFixture.create(section = section2, group = group2, groupUser = groupUser2, type = ContentType.CODE)

        val response =
            contentService.readAll(
                email = user.email,
                groupId = group.id,
                sectionId = section.id,
                name = null,
                languages = null,
                skills = null,
                writer = null,
                startDate = content.createdDate!! - 1000L,
                endDate = content.createdDate!! + 1000L,
                pageable = PageRequest.of(0, 10),
                type = null,
            )

        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun readAllGroupIdIsNull() {
        val section = sectionFixture.create(group = group)
        val content =
            contentFixture.create(section = section, group = group, groupUser = groupUser, type = ContentType.CODE)
        contentFixture.create(section = section, group = group, groupUser = groupUser, type = ContentType.BOARD)

        val response =
            contentService.readAll(
                email = user.email,
                groupId = null,
                sectionId = null,
                name = null,
                languages = null,
                skills = null,
                writer = null,
                startDate = content.createdDate!! - 1000L,
                endDate = content.createdDate!! + 1000L,
                pageable = PageRequest.of(0, 10),
                type = null,
            )

        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun read() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.CODE, group = group)

        val response =
            contentService.read(
                email = user.email,
                groupId = group.id!!,
                sectionId = section.id!!,
                contentId = content.id!!,
            )

        Assertions.assertThat(response.content).isEqualTo(content.content)
        Assertions.assertThat(response.contentId).isEqualTo(content.id)
        Assertions.assertThat(response.name).isEqualTo(content.name)
        Assertions.assertThat(response.type).isEqualTo(content.type)
    }

    @Test
    fun readSectionIsNotPublic() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY, isPublic = false)
        val visitor = userFixture.create()
        val content =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.CODE, group = group)

        Assertions
            .assertThatThrownBy {
                contentService.read(visitor.email, group.id!!, section.id!!, content.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readGroupUserIsNotActive() {
        val admin = userFixture.create()
        val group1 = groupFixture.create(user = admin, isPublic = false)
        val section = sectionFixture.create(group = group1, type = SectionType.REPOSITORY, isPublic = false)
        val user1 = userFixture.create()
        groupUserFixture.create(group = group1, user = user1, role = GroupRole.SUSPENDED)
        val content =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.CODE, group = group1)

        Assertions
            .assertThatThrownBy {
                contentService.read(user1.email, group1.id!!, section.id!!, content.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readNotFoundSection() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.CODE, group = group)

        Assertions
            .assertThatThrownBy {
                contentService.read(user.email, group.id!!, 100L, content.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readNotFoundContent() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)

        Assertions
            .assertThatThrownBy {
                contentService.read(user.email, group.id!!, section.id!!, 100L)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readBookmarkExist() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.CODE, group = group)
        val bookmark =
            bookmarkFixture.create(
                contentId = content.id!!,
                group = group,
                type = BookmarkType.CODE,
                user = user,
                section = section,
            )

        val response =
            contentService.read(
                email = user.email,
                groupId = group.id!!,
                sectionId = section.id!!,
                contentId = content.id!!,
            )

        Assertions.assertThat(response.content).isEqualTo(content.content)
        Assertions.assertThat(response.contentId).isEqualTo(content.id)
        Assertions.assertThat(response.name).isEqualTo(content.name)
        Assertions.assertThat(response.type).isEqualTo(content.type)
        Assertions.assertThat(response.bookmarkId).isEqualTo(bookmark.id)
    }

    @Test
    fun readFolders() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val parent =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.FOLDER, group = group)
        val content1 =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.FILE, group = group, parent = parent)
        val content2 =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.FOLDER, group = group, parent = parent)

        val response =
            contentService.readFolders(user.email, group.id!!, section.id!!, parent.id!!, Pageable.unpaged())

        Assertions.assertThat(response.parentId).isEqualTo(parent.id)
        Assertions.assertThat(response.contents).isNotEmpty
        Assertions.assertThat(response.contents.size).isEqualTo(2)
    }

    @Test
    fun readFoldersNotFoundParent() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)

        Assertions
            .assertThatThrownBy {
                contentService.readFolders(user.email, group.id!!, section.id!!, 0L, Pageable.unpaged())
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readFoldersGroupAndSectionIsNotPublic() {
        val visitor = userFixture.create()
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY, isPublic = false)
        val parent =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.FOLDER, group = group)

        Assertions
            .assertThatThrownBy {
                contentService.readFolders(visitor.email, group.id!!, section.id!!, parent.id!!, Pageable.unpaged())
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun create() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val request =
            ContentCreateRequest(
                name = "name",
                languages = listOf("Kotlin", "Python"),
                skills = listOf("skill"),
                content = "content",
                codeDescription = null,
                type = ContentType.BOARD,
                parentId = null,
            )

        val response =
            contentService.create(
                email = user.email,
                groupId = group.id!!,
                sectionId = section.id!!,
                request = request,
                files = null,
            )

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.type).isEqualTo(request.type)
    }

    @Test
    fun createCode() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val request =
            ContentCreateRequest(
                name = "name",
                languages = listOf("Kotlin", "Python"),
                skills = listOf("skill"),
                content = "content",
                codeDescription = "코드 설명",
                type = ContentType.CODE,
                parentId = null,
            )

        val response =
            contentService.create(
                email = user.email,
                groupId = group.id!!,
                sectionId = section.id!!,
                request = request,
                files = null,
            )

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.type).isEqualTo(request.type)
    }

    @Test
    fun createFolder() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val request =
            ContentCreateRequest(
                name = "name",
                languages = null,
                skills = null,
                content = "content",
                codeDescription = null,
                type = ContentType.FOLDER,
                parentId = null,
            )

        val response =
            contentService.create(
                email = user.email,
                groupId = group.id!!,
                sectionId = section.id!!,
                request = request,
                files = null,
            )

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.type).isEqualTo(request.type)
    }

    @Test
    fun createFolderParentExist() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val parent =
            contentFixture.create(section = section, groupUser = groupUser, type = ContentType.FOLDER, group = group)
        val request =
            ContentCreateRequest(
                name = "name",
                languages = null,
                skills = null,
                content = "content",
                codeDescription = null,
                type = ContentType.FOLDER,
                parentId = parent.id,
            )

        val file =
            MockMultipartFile(
                "이미지.png",
                "이미지.png",
                "png",
                "이미지.png".byteInputStream(),
            )

        val response =
            contentService.create(
                email = user.email,
                groupId = group.id!!,
                sectionId = section.id!!,
                request = request,
                files = listOf(file),
            )

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.type).isEqualTo(request.type)
    }

    @Test
    fun createNotFoundSection() {
        val request =
            ContentCreateRequest(
                name = "name",
                languages = null,
                skills = null,
                content = "content",
                codeDescription = null,
                type = ContentType.BOARD,
                parentId = null,
            )

        Assertions.assertThatThrownBy {
            contentService.create(
                email = user.email,
                groupId = group.id!!,
                sectionId = 100L,
                request = request,
                files = null,
            )
        }
    }

    @Test
    fun createFile() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val parent =
            contentFixture.create(group = group, groupUser = groupUser, type = ContentType.FOLDER, section = section)
        val request =
            ContentFileCreateRequest(
                parentFolderId = parent.id!!,
            )

        val file =
            MockMultipartFile(
                "이미지.png",
                "이미지.png",
                "png",
                "이미지.png".byteInputStream(),
            )

        val response =
            contentService.createFile(
                email = user.email,
                groupId = group.id!!,
                sectionId = section.id!!,
                request = request,
                files = listOf(file),
            )
    }

    @Test
    fun createFileNotFoundSection() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val parent =
            contentFixture.create(group = group, groupUser = groupUser, type = ContentType.FOLDER, section = section)
        val request =
            ContentFileCreateRequest(
                parentFolderId = parent.id!!,
            )

        val file =
            MockMultipartFile(
                "이미지.png",
                "이미지.png",
                "png",
                "이미지.png".byteInputStream(),
            )

        Assertions.assertThatThrownBy {
            contentService.createFile(
                email = user.email,
                groupId = group.id!!,
                sectionId = 0L,
                request = request,
                files = listOf(file),
            )
        }
    }

    @Test
    fun createFileNotFoundParent() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val request =
            ContentFileCreateRequest(
                parentFolderId = 0L,
            )

        val file =
            MockMultipartFile(
                "이미지.png",
                "이미지.png",
                "png",
                "이미지.png".byteInputStream(),
            )

        Assertions.assertThatThrownBy {
            contentService.createFile(
                email = user.email,
                groupId = group.id!!,
                sectionId = section.id!!,
                request = request,
                files = listOf(file),
            )
        }
    }

    @Test
    fun update() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(section = section, group = group, groupUser = groupUser, type = ContentType.BOARD)

        val request =
            ContentUpdateRequest(
                contentId = content.id!!,
                name = "content",
                languages = listOf("Java", "Kotlin"),
                skills = listOf("Skill"),
                content = "cotent",
                codeDescription = "codeDscription",
                type = ContentType.BOARD,
            )
        val file =
            MockMultipartFile(
                "이미지.png",
                "이미지.png",
                "png",
                "이미지.png".byteInputStream(),
            )
        val response = contentService.update(groupUser.user.email, group.id!!, section.id!!, request, listOf(file))

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.contentId).isEqualTo(request.contentId)
        Assertions.assertThat(response.languages?.size).isEqualTo(request.languages!!.size)
    }

    @Test
    fun updateNotFoundContent() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)

        val request =
            ContentUpdateRequest(
                contentId = 0L,
                name = "content",
                languages = listOf("Java", "Kotlin"),
                skills = null,
                content = "cotent",
                codeDescription = "codeDscription",
                type = ContentType.CODE,
            )

        Assertions.assertThatThrownBy {
            contentService.update(
                groupUser.user.email,
                group.id!!,
                section.id!!,
                request,
                null,
            )
        }
    }

    @Test
    fun delete() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(group = group, groupUser = groupUser, type = ContentType.CODE, section = section)

        contentService.delete(user.email, group.id!!, section.id!!, content.id!!)

        val response = contentRepository.findAll()
        Assertions.assertThat(response).isEmpty()
    }

    @Test
    fun deleteNotFoundSection() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(group = group, groupUser = groupUser, type = ContentType.CODE, section = section)

        Assertions
            .assertThatThrownBy {
                contentService.delete(user.email, group.id!!, 0L, content.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteWithBookmark() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content =
            contentFixture.create(group = group, groupUser = groupUser, type = ContentType.CODE, section = section)
        val bookmark =
            bookmarkFixture.create(
                contentId = content.id!!,
                group = group,
                type = BookmarkType.CODE,
                user = user,
                section = section,
            )

        contentService.delete(user.email, group.id!!, section.id!!, content.id!!)

        val response1 = contentRepository.findAll()
        val response2 = bookmarkRepository.findAll()
        Assertions.assertThat(response1).isEmpty()
        Assertions.assertThat(response2).isEmpty()
    }

    @Test
    fun readHot() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY, isPublic = true)
        val content1 =
            contentFixture.create(group = group, groupUser = groupUser, type = ContentType.CODE, section = section)
        val content2 =
            contentFixture.create(group = group, groupUser = groupUser, type = ContentType.BOARD, section = section)

        val response = contentService.readHots()

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun readHotByCache() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY, isPublic = true)
        val content1 =
            contentFixture.create(group = group, groupUser = groupUser, type = ContentType.CODE, section = section)
        val content2 =
            contentFixture.create(group = group, groupUser = groupUser, type = ContentType.BOARD, section = section)

        contentService.readHots()
        val response = contentService.readHots()

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response.size).isEqualTo(2)
    }
}
