package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.fixture.GroupFixture
import com.project.api.fixture.GroupUserFixture
import com.project.api.fixture.SectionFixture
import com.project.api.fixture.UserFixture
import com.project.api.repository.category.SectionNotificationRepository
import com.project.api.repository.category.SectionRepository
import com.project.api.web.dto.request.CategoryUpdateRequest
import com.project.api.web.dto.request.SectionCreateRequest
import com.project.core.internal.CategoryNotificationType
import com.project.core.internal.SectionType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class SectionServiceTest(
    @Autowired private val sectionFixture: SectionFixture,
    @Autowired private val sectionService: SectionService,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val sectionNotificationRepository: SectionNotificationRepository,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val sectionRepository: SectionRepository,
    @Autowired private val groupUserFixture: GroupUserFixture,
) {
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
    fun readAll() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val section1 = sectionFixture.create(group = group)
        val section2 = sectionFixture.create(group = group)

        val response =
            sectionService.readAll(
                email = user.email,
                pageable = Pageable.unpaged(),
                groupId = group.id!!,
                parentSectionId = null,
            )

        Assertions.assertThat(response!!.size).isEqualTo(2)
        Assertions.assertThat(response[0].type).isEqualTo(SectionType.MENU)
        Assertions.assertThat(response[1].type).isEqualTo(SectionType.MENU)
    }

    @Test
    fun readAllChildSection() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val section1 = sectionFixture.create(group = group)
        val section2 = sectionFixture.create(group = group, parent = section1)

        val response =
            sectionService.readAll(
                email = user.email,
                pageable = Pageable.unpaged(),
                groupId = group.id!!,
                parentSectionId = section1.id,
            )

        Assertions.assertThat(response!!.size).isEqualTo(1)
        Assertions.assertThat(response[0].type).isEqualTo(SectionType.MENU)
        Assertions.assertThat(response[0].isPublic).isEqualTo(section1.isPublic)
    }

    @Test
    fun createCategory() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val request =
            SectionCreateRequest(
                name = "folder",
                groupId = group.id!!,
                isPublic = true,
                parentSectionId = null,
                type = SectionType.MENU,
            )

        val response =
            sectionService.create(
                email = user.email,
                request = request,
            )

        val notificationResponse = sectionNotificationRepository.findAll()

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.groupId).isEqualTo(group.id)
        Assertions.assertThat(response.isPublic).isEqualTo(request.isPublic)
        Assertions.assertThat(response.type).isEqualTo(request.type)
        Assertions.assertThat(notificationResponse).isNotEmpty
        Assertions.assertThat(notificationResponse[0].type).isEqualTo(CategoryNotificationType.ALL)
    }

    @Test
    fun createSubCategory() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val section =
            sectionFixture.create(
                group = group,
            )
        val request =
            SectionCreateRequest(
                name = "folder",
                groupId = group.id!!,
                isPublic = null,
                parentSectionId = section.id,
                type = SectionType.MENU,
            )

        val response =
            sectionService.create(
                email = user.email,
                request = request,
            )

        val notificationResponse = sectionNotificationRepository.findAll()

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.groupId).isEqualTo(group.id)
        Assertions.assertThat(response.isPublic).isEqualTo(section.isPublic)
        Assertions.assertThat(response.type).isEqualTo(request.type)
        Assertions.assertThat(notificationResponse).isEmpty()
    }

    @Test
    fun createNotFoundGroup() {
        val user = userFixture.create()
        val request =
            SectionCreateRequest(
                name = "folder",
                groupId = 1L,
                isPublic = null,
                parentSectionId = null,
                type = SectionType.MENU,
            )

        Assertions
            .assertThatThrownBy {
                sectionService.create(
                    email = user.email,
                    request = request,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun createNotFoundGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()

        val request =
            SectionCreateRequest(
                name = "folder",
                groupId = group.id!!,
                isPublic = null,
                parentSectionId = null,
                type = SectionType.MENU,
            )

        Assertions
            .assertThatThrownBy {
                sectionService.create(
                    email = user.email,
                    request = request,
                )
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun createNotAllowedIfRoleIsNotTopAdmin() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        val groupUser = groupUserFixture.create(group = group, user = user)

        val request =
            SectionCreateRequest(
                name = "folder",
                groupId = group.id!!,
                isPublic = null,
                parentSectionId = null,
                type = SectionType.MENU,
            )

        Assertions
            .assertThatThrownBy {
                sectionService.create(email = user.email, request = request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateCategory() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val section = sectionFixture.create(group = group)
        val request =
            CategoryUpdateRequest(
                name = "folder",
                groupId = group.id!!,
                isPublic = true,
                categoryId = section.id!!,
            )

        val response = sectionService.update(email = user.email, request = request)

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.groupId).isEqualTo(group.id)
        Assertions.assertThat(response.isPublic).isEqualTo(request.isPublic)
        Assertions.assertThat(response.categoryId).isEqualTo(section.id)
    }

    @Test
    fun updateNotFoundGroup() {
        val user = userFixture.create()
        val request =
            CategoryUpdateRequest(
                name = "category",
                groupId = 1L,
                isPublic = true,
                categoryId = 1L,
            )

        Assertions
            .assertThatThrownBy {
                sectionService.update(email = user.email, request = request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateNotFoundGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        val category = sectionFixture.create(group = group)
        val request =
            CategoryUpdateRequest(
                name = "folder",
                groupId = group.id!!,
                isPublic = null,
                categoryId = category.id!!,
            )

        Assertions
            .assertThatThrownBy {
                sectionService.update(email = user.email, request = request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateNotAllowedIfRoleIsNotTopAdmin() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        groupUserFixture.create(group = group, user = user)
        val category = sectionFixture.create(group = group)
        val request =
            CategoryUpdateRequest(
                name = "folder",
                groupId = group.id!!,
                isPublic = true,
                categoryId = category.id!!,
            )

        Assertions
            .assertThatThrownBy {
                sectionService.update(email = user.email, request = request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateNotFoundCategory() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val request =
            CategoryUpdateRequest(
                name = "folder",
                groupId = group.id!!,
                isPublic = true,
                categoryId = 1L,
            )

        Assertions
            .assertThatThrownBy {
                sectionService.update(email = admin.email, request = request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun delete() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val category = sectionFixture.create(group = group)

        sectionService.delete(email = user.email, sectionId = category.id!!)

        val categoryResponse = sectionRepository.findAll()
        val notificationResponse = sectionNotificationRepository.findAll()

        Assertions.assertThat(categoryResponse).isEmpty()
        Assertions.assertThat(notificationResponse).isEmpty()
    }

    @Test
    fun deleteNotFoundCategory() {
        val user = userFixture.create()
        val group = groupFixture.create(user)

        Assertions
            .assertThatThrownBy {
                sectionService.delete(email = user.email, sectionId = 1L)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteNotFoundGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        val category = sectionFixture.create(group = group)

        Assertions
            .assertThatThrownBy {
                sectionService.delete(email = user.email, sectionId = category.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteNotAllowedIfRoleIsNotTopAdmin() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        groupUserFixture.create(group = group, user = user)
        val category = sectionFixture.create(group = group)

        Assertions
            .assertThatThrownBy {
                sectionService.delete(email = user.email, sectionId = category.id!!)
            }.isInstanceOf(RestException::class.java)
    }
}
