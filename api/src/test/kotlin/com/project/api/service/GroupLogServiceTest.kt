package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.repository.group.GroupLogRepository
import com.project.api.supprot.fixture.ContentFixture
import com.project.api.supprot.fixture.GroupFixture
import com.project.api.supprot.fixture.GroupUserFixture
import com.project.api.supprot.fixture.SectionFixture
import com.project.api.supprot.fixture.UserFixture
import com.project.core.domain.group.GroupLog
import com.project.core.internal.ContentType
import com.project.core.internal.GroupRole
import com.project.core.internal.SectionType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable

class GroupLogServiceTest(
    @Autowired private val groupLogRepository: GroupLogRepository,
    @Autowired private val groupLogService: GroupLogService,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val sectionFixture: SectionFixture,
    @Autowired private val contentFixture: ContentFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
) : TestCommonSetting() {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        groupLogRepository.deleteAll()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun readAll() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = true)
        val user2 = userFixture.create()
        val groupUser = groupUserFixture.create(user = user2, group = group)
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)

        val groupLog =
            groupLogRepository.save(
                GroupLog(
                    user = user2,
                    group = group,
                    type = ContentType.BOARD,
                    contentId = content.id!!,
                    contentName = content.name,
                    sectionId = section.id!!,
                ),
            )

        val response =
            groupLogService.readAll(
                email = user2.email,
                pageable = Pageable.unpaged(),
                groupId = group.id!!,
                type = ContentType.BOARD,
            )

        Assertions.assertThat(response.creatorId).isEqualTo(user.id)
        Assertions.assertThat(response.creatorName).isEqualTo(user.name)
        Assertions.assertThat(response.creatorImg).isEqualTo(user.img)
        Assertions.assertThat(response.logs).isNotEmpty
    }

    @Test
    fun readAllGroupIsNotPublic() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = false)
        val user2 = userFixture.create()
        val groupUser = groupUserFixture.create(user = user2, group = group)
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)

        val groupLog =
            groupLogRepository.save(
                GroupLog(
                    user = user2,
                    group = group,
                    type = ContentType.BOARD,
                    contentId = content.id!!,
                    contentName = content.name,
                    sectionId = section.id!!,
                ),
            )

        val response =
            groupLogService.readAll(
                email = user2.email,
                pageable = Pageable.unpaged(),
                groupId = group.id!!,
                type = ContentType.BOARD,
            )

        Assertions.assertThat(response.creatorId).isEqualTo(user.id)
        Assertions.assertThat(response.creatorName).isEqualTo(user.name)
        Assertions.assertThat(response.creatorImg).isEqualTo(user.img)
        Assertions.assertThat(response.logs).isNotEmpty
    }

    @Test
    fun readAllAllType() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = false)
        val user2 = userFixture.create()
        val groupUser = groupUserFixture.create(user = user2, group = group)
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)

        val groupLog =
            groupLogRepository.save(
                GroupLog(
                    user = user2,
                    group = group,
                    type = ContentType.BOARD,
                    contentId = content.id!!,
                    contentName = content.name,
                    sectionId = section.id!!,
                ),
            )

        val response =
            groupLogService.readAll(
                email = user2.email,
                pageable = Pageable.unpaged(),
                groupId = group.id!!,
                type = null,
            )

        Assertions.assertThat(response.creatorId).isEqualTo(user.id)
        Assertions.assertThat(response.creatorName).isEqualTo(user.name)
        Assertions.assertThat(response.creatorImg).isEqualTo(user.img)
        Assertions.assertThat(response.logs).isNotEmpty
    }

    @Test
    fun readAllRoleIsNotActive() {
        val user = userFixture.create()
        val group = groupFixture.create(user = user, isPublic = false)
        val user2 = userFixture.create()
        val groupUser = groupUserFixture.create(user = user2, group = group, role = GroupRole.SUSPENDED)
        val section = sectionFixture.create(group = group, type = SectionType.REPOSITORY)
        val content = contentFixture.create(section = section, groupUser = groupUser, group = group)

        val groupLog =
            groupLogRepository.save(
                GroupLog(
                    user = user2,
                    group = group,
                    type = ContentType.BOARD,
                    contentId = content.id!!,
                    contentName = content.name,
                    sectionId = section.id!!,
                ),
            )

        Assertions
            .assertThatThrownBy {
                groupLogService.readAll(
                    email = user2.email,
                    pageable = Pageable.unpaged(),
                    groupId = group.id!!,
                    type = null,
                )
            }.isInstanceOf(RestException::class.java)
    }
}
