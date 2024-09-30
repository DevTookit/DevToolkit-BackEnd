package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.repository.category.SectionNotificationRepository
import com.project.api.repository.category.SectionRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.supprot.fixture.GroupFixture
import com.project.api.supprot.fixture.GroupUserFixture
import com.project.api.supprot.fixture.SectionFixture
import com.project.api.supprot.fixture.UserFixture
import com.project.api.web.dto.request.SectionNotificationUpdateRequest
import com.project.core.internal.GroupRole
import com.project.core.internal.SectionNotificationType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

class SectionNotificationServiceTest(
    @Autowired private val sectionFixture: SectionFixture,
    @Autowired private val sectionService: SectionService,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val sectionNotificationRepository: SectionNotificationRepository,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val sectionRepository: SectionRepository,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val sectionNotificationService: SectionNotificationService,
    @Autowired private val groupUserRepository: GroupUserRepository,
): TestCommonSetting()  {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        sectionFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun updateByEmail() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val category = sectionFixture.create(group = group)
        val request =
            SectionNotificationUpdateRequest(
                groupId = group.id!!,
                sectionId = category.id!!,
                type = SectionNotificationType.NONE,
            )

        val response = sectionNotificationService.update(user.email, request)

        Assertions.assertThat(response.categoryId).isEqualTo(category.id)
        Assertions.assertThat(response.type).isEqualTo(request.type)
    }

    @Test
    fun updateByEmailNotFoundGroup() {
        val user = userFixture.create()
        val request =
            SectionNotificationUpdateRequest(
                groupId = 1L,
                sectionId = 1L,
                type = SectionNotificationType.NONE,
            )

        Assertions
            .assertThatThrownBy {
                sectionNotificationService.update(user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateByEmailNotFoundGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        val category = sectionFixture.create(group = group)
        val request =
            SectionNotificationUpdateRequest(
                groupId = group.id!!,
                sectionId = category.id!!,
                type = SectionNotificationType.NONE,
            )

        Assertions
            .assertThatThrownBy {
                sectionNotificationService.update(user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateByEmailNotAllowedIfRoleIsNotTopAdmin() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        groupUserFixture.create(group = group, user = user, role = GroupRole.PENDING)
        val category = sectionFixture.create(group = group)
        val request =
            SectionNotificationUpdateRequest(
                groupId = group.id!!,
                sectionId = category.id!!,
                type = SectionNotificationType.NONE,
            )

        Assertions
            .assertThatThrownBy {
                sectionNotificationService.update(user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateByEmailNotFoundCategory() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        groupUserFixture.create(group, user)
        val request =
            SectionNotificationUpdateRequest(
                groupId = group.id!!,
                sectionId = 1L,
                type = SectionNotificationType.NONE,
            )

        Assertions
            .assertThatThrownBy {
                sectionNotificationService.update(user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateByGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val groupAdmin = groupUserRepository.findByUserAndGroup(admin, group)!!
        val category = sectionFixture.create(group = group)

        sectionNotificationService.update(group = group, groupUser = groupAdmin, type = SectionNotificationType.MENTIONS)

        val response = sectionNotificationRepository.findAll()
        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].type).isEqualTo(SectionNotificationType.MENTIONS)
    }
}
