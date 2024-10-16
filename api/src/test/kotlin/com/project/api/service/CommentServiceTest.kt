package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.repository.announcement.AnnouncementRepository
import com.project.api.repository.comment.CommentRepository
import com.project.api.supprot.fixture.CommentFixture
import com.project.api.supprot.fixture.CommentMentionFixture
import com.project.api.supprot.fixture.ContentFixture
import com.project.api.supprot.fixture.GroupFixture
import com.project.api.supprot.fixture.GroupUserFixture
import com.project.api.supprot.fixture.SectionFixture
import com.project.api.supprot.fixture.UserFixture
import com.project.api.web.dto.request.CommentCreateMentionRequest
import com.project.api.web.dto.request.CommentCreateRequest
import com.project.api.web.dto.request.CommentUpdateRequest
import com.project.core.domain.announcement.Announcement
import com.project.core.domain.comment.CommentMention
import com.project.core.domain.group.Group
import com.project.core.domain.group.GroupUser
import com.project.core.domain.user.User
import com.project.core.internal.CommentType
import com.project.core.internal.GroupRole
import com.project.core.internal.SectionType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CommentServiceTest(
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val commentService: CommentService,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val announcementRepository: AnnouncementRepository,
    @Autowired private val commentFixture: CommentFixture,
    @Autowired private val commentMentionFixture: CommentMentionFixture,
    @Autowired private val sectionFixture: SectionFixture,
    @Autowired private val contentFixture: ContentFixture,
    @Autowired private val commentRepository: CommentRepository,
) : TestCommonSetting() {
    lateinit var user: User
    lateinit var group: Group
    lateinit var groupUser: GroupUser

    @BeforeEach
    fun setUp() {
        user = userFixture.create()
        group = groupFixture.create(user = user)
        groupUser = groupUserFixture.create(user = userFixture.create(), group = group)
    }

    @AfterEach
    fun tearDown() {
        commentMentionFixture.tearDown()
        commentFixture.tearDown()
        contentFixture.tearDown()
        announcementRepository.deleteAll()
        groupUserFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun readAll() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)
        val comment = commentFixture.create(contentId = content.id!!, groupUser = groupUser, group = group)
        val commentMention = commentMentionFixture.create(comment = comment, groupUser = groupUser)
        comment.apply { this.mentions.add(commentMention) }

        val response =
            commentService.readAll(
                email = user.email,
                groupId = group.id!!,
                contentId = content.id!!,
            )

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].commentId).isEqualTo(comment.id)
        Assertions.assertThat(response[0].content).isEqualTo(comment.content)
        Assertions.assertThat(response[0].mentions).isNotEmpty
        Assertions.assertThat(response[0].mentions!![0].groupUserId).isEqualTo(groupUser.id)
    }

    @Test
    fun readAllNotFoundContent() {
        Assertions
            .assertThatThrownBy {
                commentService.readAll(
                    email = user.email,
                    groupId = group.id!!,
                    contentId = 0L,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun createTypeIsContent() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)
        val request =
            CommentCreateRequest(
                groupId = group.id!!,
                contentId = content.id!!,
                content = "Comment",
                mentions = null,
                type = CommentType.CONTENT,
            )

        val response = commentService.create(groupUser.user.email, request)

        Assertions.assertThat(response.content).isEqualTo(response.content)
        Assertions.assertThat(response.writerId).isEqualTo(groupUser.id)
    }

    @Test
    fun createTypeIsAnnounce() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val announce =
            announcementRepository.save(
                Announcement(
                    group = group,
                    groupUser =
                        groupUserFixture.create(
                            user = userFixture.create(),
                            role = GroupRole.TOP_MANAGER,
                            group = group,
                        ),
                    content = "공지사항",
                    name = "공지사항",
                ),
            )
        val request =
            CommentCreateRequest(
                groupId = group.id!!,
                contentId = announce.id!!,
                content = "Comment",
                mentions = null,
                type = CommentType.ANNOUNCE,
            )

        val response = commentService.create(groupUser.user.email, request)

        Assertions.assertThat(response.content).isEqualTo(response.content)
        Assertions.assertThat(response.writerId).isEqualTo(groupUser.id)
    }

    @Test
    fun createNotFoundContent() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val request =
            CommentCreateRequest(
                groupId = group.id!!,
                contentId = 0L,
                content = "Comment",
                mentions = null,
                type = CommentType.CONTENT,
            )

        Assertions
            .assertThatThrownBy {
                commentService.create(groupUser.user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun createNotFoundAnnounce() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val request =
            CommentCreateRequest(
                groupId = group.id!!,
                contentId = 0L,
                content = "Comment",
                mentions = null,
                type = CommentType.ANNOUNCE,
            )

        Assertions
            .assertThatThrownBy {
                commentService.create(groupUser.user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun createNotFoundGroupUsers() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)
        val request =
            CommentCreateRequest(
                groupId = group.id!!,
                contentId = content.id!!,
                content = "Comment",
                mentions = null,
                type = CommentType.CONTENT,
            )
        val visitor = userFixture.create()
        Assertions.assertThatThrownBy {
            commentService.create(visitor.email, request)
        }
    }

    @Test
    fun update() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)
        val comment =
            commentFixture.create(
                groupUser = groupUser,
                group = group,
                type = CommentType.CONTENT,
                contentId = content.id!!,
            )
        val commentMention = commentMentionFixture.create(comment = comment, groupUser = groupUser)
        comment.apply { this.mentions.add(commentMention) }
        val groupUser1 = groupUserFixture.create(group = group,user= userFixture.create())

        val request =
            CommentUpdateRequest(
                groupId = group.id!!,
                contentId = content.id!!,
                commentId = comment.id!!,
                content = "댓글 수정",
                mentions = listOf(CommentCreateMentionRequest(groupUserId = groupUser1.id!!)),
            )

        val response = commentService.update(groupUser.user.email, request)
        Assertions.assertThat(response.content).isEqualTo(request.content)
        Assertions.assertThat(response.writerId).isEqualTo(groupUser.id)
        Assertions.assertThat(response.commentId).isEqualTo(request.commentId)
        Assertions.assertThat(response.mentionsIds).isNotEmpty
        Assertions.assertThat(response.mentionsIds!![0]).isEqualTo(groupUser1.id)
    }

    @Test
    fun updateNotFoundComment() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)
        val comment =
            commentFixture.create(
                groupUser = groupUser,
                group = group,
                type = CommentType.CONTENT,
                contentId = content.id!!,
            )
        val request =
            CommentUpdateRequest(
                groupId = group.id!!,
                contentId = content.id!!,
                commentId = 0L,
                content = "댓글 수정",
                mentions = null,
            )
        Assertions
            .assertThatThrownBy {
                commentService.update(groupUser.user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updatePossibleOnlyWriter() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)
        val comment =
            commentFixture.create(
                groupUser = groupUser,
                group = group,
                type = CommentType.CONTENT,
                contentId = content.id!!,
            )

        val request =
            CommentUpdateRequest(
                groupId = group.id!!,
                contentId = content.id!!,
                commentId = 0L,
                content = "댓글 수정",
                mentions = null,
            )

        Assertions
            .assertThatThrownBy {
                commentService.update(user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun delete() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)
        val comment =
            commentFixture.create(
                groupUser = groupUser,
                group = group,
                type = CommentType.CONTENT,
                contentId = content.id!!,
            )

        commentService.delete(groupUser.user.email, groupId = group.id!!, contentId = content.id!!, commentId = comment.id!!)

        val response = commentRepository.findAll()
        Assertions.assertThat(response).isEmpty()
    }

    @Test
    fun deleteNotFoundComment() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)

        Assertions
            .assertThatThrownBy {
                commentService.delete(groupUser.user.email, groupId = group.id!!, contentId = content.id!!, commentId = 0L)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deletePossibleWriterAndAdmin() {
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)
        val comment =
            commentFixture.create(
                groupUser = groupUser,
                group = group,
                type = CommentType.CONTENT,
                contentId = content.id!!,
            )
        val user1 = userFixture.create()
        val groupUser1 = groupUserFixture.create(user = user1, group = group)

        Assertions
            .assertThatThrownBy {
                commentService.delete(groupUser1.user.email, groupId = group.id!!, contentId = content.id!!, commentId = comment.id!!)
            }.isInstanceOf(RestException::class.java)
    }
}
