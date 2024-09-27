package com.project.api.service

import com.project.api.fixture.ContentFixture
import com.project.api.fixture.FolderFixture
import com.project.api.fixture.GroupFixture
import com.project.api.fixture.GroupUserFixture
import com.project.api.fixture.SectionFixture
import com.project.api.fixture.UserFixture
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class FolderContentServiceTest(
    @Autowired private val folderContentService: FolderContentService,
    @Autowired private val folderFixture: FolderFixture,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val sectionFixture: SectionFixture,
    @Autowired private val contentFixture: ContentFixture,
) {
/*    lateinit var user: User
    lateinit var group: Group

    @BeforeEach
    fun setUp() {
        user = userFixture.create()
        group = groupFixture.create(user)
    }

    @AfterEach
    fun tearDown() {
        folderFixture.tearDown()
        sectionFixture.tearDown()
        groupUserFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun readAll() {
        val section = sectionFixture.create(group = group, type = REPOSITORY)
        val folder =
            folderFixture.create(
                group = group,
                section = section,
            )

        val response =
            folderContentService.readAll(
                email = user.email,
                pageable = Pageable.unpaged(),
                groupId = group.id!!,
                sectionId = section.id!!,
                parentFolderId = null,
            )

        Assertions.assertThat(response.parentId).isNull()
        Assertions.assertThat(response.name).isNull()
        Assertions.assertThat(response.subFolders!!.size).isEqualTo(1)
        Assertions.assertThat(response.subFolders!![0].id).isEqualTo(folder.id)
        Assertions.assertThat(response.subFolders!![0].name).isEqualTo(folder.name)
    }

    @Test
    fun readAllAttachmentsExist() {
        val section = sectionFixture.create(group = group, type = REPOSITORY)
        val folder =
            folderFixture.create(
                group = group,
                section = section,
            )
        val folder1 =
            folderFixture.create(
                group = group,
                section = section,
                parent = folder,
            )
        val folderAttachment = folderAttachmentFixture.create(folder = folder)

        val response =
            folderContentService.readAll(
                email = user.email,
                pageable = Pageable.unpaged(),
                groupId = group.id!!,
                sectionId = section.id!!,
                parentFolderId = folder.id,
            )
        Assertions.assertThat(response.name).isEqualTo(folder.name)
        Assertions.assertThat(response.parentId).isEqualTo(folder.id)
        Assertions.assertThat(response.subFolders!!.size).isEqualTo(1)
        Assertions.assertThat(response.attachments!!.size).isEqualTo(1)
    }

    @Test
    fun readAllNotAllowedTypeIsMenu() {
        val section = sectionFixture.create(group = group, type = MENU)

        Assertions
            .assertThatThrownBy {
                folderContentService.readAll(
                    email = user.email,
                    pageable = Pageable.unpaged(),
                    groupId = group.id!!,
                    sectionId = section.id!!,
                    parentFolderId = null,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun create() {
        val section = sectionFixture.create(group = group, type = REPOSITORY)
        val request =
            FolderCreateRequest(
                parentFolderId = null,
                name = "folder",
                groupId = group.id!!,
                sectionId = section.id!!,
            )

        val response =
            folderContentService.create(
                email = user.email,
                request = request,
                files = null,
            )

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.attachments).isEmpty()
    }

    @Test
    fun createGroupUserRoleIsNotActive() {
        val user1 = userFixture.create()
        val groupUser = groupUserFixture.create(group, user1, GroupRole.PENDING)
        val section = sectionFixture.create(group = group, type = REPOSITORY)

        val request =
            FolderCreateRequest(
                parentFolderId = null,
                name = "folder",
                groupId = group.id!!,
                sectionId = section.id!!,
            )

        Assertions
            .assertThatThrownBy {
                folderContentService.create(
                    email = user1.email,
                    request = request,
                    files = null,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun createParentFolderExist() {
        val section = sectionFixture.create(group = group, type = REPOSITORY)
        val folder = folderFixture.create(group = group, section = section)

        val request =
            FolderCreateRequest(
                parentFolderId = folder.id,
                name = "folder",
                groupId = group.id!!,
                sectionId = section.id!!,
            )

        val response =
            folderContentService.create(
                email = user.email,
                request = request,
                files = null,
            )

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.attachments).isEmpty()
    }

    @Test
    fun createNotAllowedTypeIsMenu() {
        val section = sectionFixture.create(group = group, type = MENU)
        val folder = folderFixture.create(group = group, section = section)

        val request =
            FolderCreateRequest(
                parentFolderId = folder.id,
                name = "folder",
                groupId = group.id!!,
                sectionId = section.id!!,
            )

        Assertions
            .assertThatThrownBy {
                folderContentService.create(
                    email = user.email,
                    request = request,
                    files = null,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun update() {
        val section = sectionFixture.create(group = group, type = REPOSITORY)
        val folder = folderFixture.create(section = section, group = group)
        val request =
            FolderUpdateRequest(
                folderId = folder.id!!,
                name = "folder",
                groupId = group.id!!,
            )

        val response =
            folderContentService.update(
                email = user.email,
                request = request,
            )

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.parentId).isEqualTo(folder.id)
    }

    @Test
    fun updateNotFoundFolder() {
        val section = sectionFixture.create(group = group, type = REPOSITORY)
        val request =
            FolderUpdateRequest(
                folderId = 1L,
                name = "folder",
                groupId = group.id!!,
            )

        Assertions
            .assertThatThrownBy {
                folderContentService.update(
                    email = user.email,
                    request = request,
                )
            }.isInstanceOf(RestException::class.java)
    }*/
}
