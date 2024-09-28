package com.project.api.service

import com.project.api.fixture.BookmarkFixture
import com.project.api.fixture.ContentFixture
import com.project.api.fixture.FolderFixture
import com.project.api.fixture.GroupFixture
import com.project.api.fixture.GroupUserFixture
import com.project.api.fixture.SectionFixture
import com.project.api.fixture.UserFixture
import com.project.api.repository.bookmark.BookmarkRepository
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class BookmarkServiceTest(
    @Autowired private val bookmarkService: BookmarkService,
    @Autowired private val bookmarkFixture: BookmarkFixture,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val sectionFixture: SectionFixture,
    @Autowired private val folderFixture: FolderFixture,
    @Autowired private val bookmarkRepository: BookmarkRepository,
    @Autowired private val contentFixture: ContentFixture,
) {
/*    lateinit var user: User
    lateinit var group: Group
    lateinit var section: Section
    lateinit var groupUser: GroupUser

    @BeforeEach
    fun setUp() {
        user = userFixture.create()
        group = groupFixture.create(user)
        groupUser = groupUserFixture.create(user = userFixture.create(), group = group)
        section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
    }

    @AfterEach
    fun tearDown() {
        bookmarkFixture.tearDown()
        folderFixture.tearDown()
        contentFixture.tearDown()
        sectionFixture.tearDown()
        groupUserFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun readAll() {
        val folder = folderFixture.create(section = section, group = group)
        val folderAttachment =
            contentFixture.create(
                groupUser = groupUser,
                group = group,
                section = section,
                type = ContentType.FILE,
                folder = folder,
            )
        bookmarkFixture.create(
            contentId = folder.id!!,
            group = group,
            user = user,
            type = BookmarkType.FOLDER,
        )
        bookmarkFixture.create(
            contentId = folderAttachment.id!!,
            group = group,
            user = user,
            type = BookmarkType.FILE,
        )
        val response =
            bookmarkService.readAll(
                email = user.email,
                groupId = group.id!!,
                type = null,
                pageable = Pageable.unpaged(),
            )

        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun readAllTypeIsFolder() {
        val folder = folderFixture.create(section = section, group = group)
        bookmarkFixture.create(
            contentId = folder.id!!,
            group = group,
            user = user,
            type = BookmarkType.FOLDER,
        )
        val response =
            bookmarkService.readAll(
                email = user.email,
                groupId = group.id!!,
                type = BookmarkType.FOLDER,
                pageable = Pageable.unpaged(),
            )

        Assertions.assertThat(response.size).isEqualTo(1)
        Assertions.assertThat(response[0].contentId).isEqualTo(folder.id!!)
        Assertions.assertThat(response[0].name).isEqualTo(folder.name)
        Assertions.assertThat(response[0].type).isEqualTo(BookmarkType.FOLDER)
    }

    @Test
    fun readAllTypeIsFile() {
        val folder = folderFixture.create(section = section, group = group)
        val folderAttachment =
            contentFixture.create(
                groupUser = groupUser,
                group = group,
                section = section,
                type = ContentType.FILE,
                folder = folder,
            )
        bookmarkFixture.create(
            contentId = folder.id!!,
            group = group,
            user = user,
            type = BookmarkType.FOLDER,
        )
        bookmarkFixture.create(
            contentId = folderAttachment.id!!,
            group = group,
            user = user,
            type = BookmarkType.FILE,
        )

        val response =
            bookmarkService.readAll(
                email = user.email,
                groupId = group.id!!,
                type = BookmarkType.FILE,
                pageable = Pageable.unpaged(),
            )

        Assertions.assertThat(response.size).isEqualTo(1)
        Assertions.assertThat(response[0].contentId).isEqualTo(folderAttachment.id!!)
        Assertions.assertThat(response[0].name).isEqualTo(folderAttachment.name)
        Assertions.assertThat(response[0].type).isEqualTo(BookmarkType.FILE)
    }

    @Test
    fun readAllTypIsCode() {
        // TODO: 컨텐츠 생성 코드 후
    }

    @Test
    fun readAllTypeIsBoard() {
        // TODO: 컨텐츠 생성 코드 후
    }

    @Test
    fun readAllNotFoundFolder() {
        val folder = folderFixture.create(section = section, group = group)
        val folderAttachment =
            contentFixture.create(
                groupUser = groupUser,
                group = group,
                section = section,
                type = ContentType.FILE,
                folder = folder,
            )
        bookmarkFixture.create(
            contentId = 100L,
            group = group,
            user = user,
            type = BookmarkType.FOLDER,
        )
        bookmarkFixture.create(
            contentId = folderAttachment.id!!,
            group = group,
            user = user,
            type = BookmarkType.FILE,
        )

        Assertions
            .assertThatThrownBy {
                bookmarkService.readAll(
                    email = user.email,
                    groupId = group.id!!,
                    type = BookmarkType.FOLDER,
                    pageable = Pageable.unpaged(),
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readAllNotFoundFile() {
        val folder = folderFixture.create(section = section, group = group)
        bookmarkFixture.create(
            contentId = 1L,
            group = group,
            user = user,
            type = BookmarkType.FOLDER,
        )
        bookmarkFixture.create(
            contentId = 1L,
            group = group,
            user = user,
            type = BookmarkType.FILE,
        )
        Assertions
            .assertThatThrownBy {
                bookmarkService.readAll(
                    email = user.email,
                    groupId = group.id!!,
                    type = BookmarkType.FILE,
                    pageable = Pageable.unpaged(),
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun create() {
        val folder = folderFixture.create(section = section, group = group)
        val request =
            BookmarkCreateRequest(
                groupId = group.id!!,
                type = BookmarkType.FOLDER,
                contentId = folder.id!!,
            )

        val response =
            bookmarkService.create(
                email = user.email,
                request = request,
            )

        val result = bookmarkRepository.findAll()
        Assertions.assertThat(result.size).isEqualTo(1)
        Assertions.assertThat(result[0].id).isEqualTo(response.bookmarkId)
        Assertions.assertThat(result[0].type).isEqualTo(response.type)
    }

    @Test
    fun createNotFoundFolder() {
        val request =
            BookmarkCreateRequest(
                groupId = group.id!!,
                type = BookmarkType.FOLDER,
                contentId = 1L,
            )

        Assertions
            .assertThatThrownBy {
                bookmarkService.create(
                    email = user.email,
                    request = request,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun createNotFoundFile() {
        val request =
            BookmarkCreateRequest(
                groupId = group.id!!,
                type = BookmarkType.FILE,
                contentId = 1L,
            )

        Assertions
            .assertThatThrownBy {
                bookmarkService.create(
                    email = user.email,
                    request = request,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun delete() {
        val folder = folderFixture.create(section = section, group = group)
        val bookmark =
            bookmarkFixture.create(
                contentId = folder.id!!,
                group = group,
                user = user,
                type = BookmarkType.FOLDER,
            )

        bookmarkService.delete(
            email = user.email,
            groupId = group.id!!,
            bookmarkId = bookmark.id!!,
        )

        val response = bookmarkRepository.findAll()
        Assertions.assertThat(response).isEmpty()
    }

    @Test
    fun deleteNotFoundBookmark() {
        Assertions
            .assertThatThrownBy {
                bookmarkService.delete(
                    email = user.email,
                    groupId = group.id!!,
                    bookmarkId = 1L,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteNotFoundGroup() {
        val folder = folderFixture.create(section = section, group = group)
        val bookmark =
            bookmarkFixture.create(
                contentId = folder.id!!,
                group = group,
                user = user,
                type = BookmarkType.FOLDER,
            )

        Assertions
            .assertThatThrownBy {
                bookmarkService.delete(
                    email = user.email,
                    groupId = 100L,
                    bookmarkId = bookmark.id!!,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteNotFoundGroupUser() {
        val user1 = userFixture.create()
        val folder = folderFixture.create(section = section, group = group)
        val bookmark =
            bookmarkFixture.create(
                contentId = folder.id!!,
                group = group,
                user = user1,
                type = BookmarkType.FOLDER,
            )

        Assertions
            .assertThatThrownBy {
                bookmarkService.delete(
                    email = user1.email,
                    groupId = 1L,
                    bookmarkId = bookmark.id!!,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteNotAllowedGroupRoleIsNotActive() {
        val user1 = userFixture.create()
        val groupUser = groupUserFixture.create(group, user1, GroupRole.PENDING)
        val folder = folderFixture.create(section = section, group = group)
        val bookmark =
            bookmarkFixture.create(
                contentId = folder.id!!,
                group = group,
                user = user1,
                type = BookmarkType.FOLDER,
            )

        Assertions
            .assertThatThrownBy {
                bookmarkService.delete(
                    email = user1.email,
                    groupId = 1L,
                    bookmarkId = bookmark.id!!,
                )
            }.isInstanceOf(RestException::class.java)
    }*/
}
