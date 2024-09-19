package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.fixture.CategoryFixture
import com.project.api.fixture.GroupFixture
import com.project.api.fixture.GroupUserFixture
import com.project.api.fixture.UserFixture
import com.project.api.repository.category.CategoryNotificationRepository
import com.project.api.repository.category.CategoryRepository
import com.project.api.repository.group.GroupUserRepository
import com.project.api.web.dto.request.CategoryNotificationUpdateRequest
import com.project.core.domain.user.QUser.user
import com.project.core.internal.CategoryNotificationType
import com.project.core.internal.GroupRole
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class CategoryNotificationServiceTest(
    @Autowired private val categoryFixture: CategoryFixture,
    @Autowired private val categoryService: CategoryService,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val categoryNotificationRepository: CategoryNotificationRepository,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val categoryRepository: CategoryRepository,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val categoryNotificationService: CategoryNotificationService,
    @Autowired private val groupUserRepository: GroupUserRepository,
) {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        categoryFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun updateByEmail() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val category = categoryFixture.create(group = group)
        val request =
            CategoryNotificationUpdateRequest(
                groupId = group.id!!,
                categoryId = category.id!!,
                type = CategoryNotificationType.NONE,
            )

        val response = categoryNotificationService.update(user.email, request)

        Assertions.assertThat(response.categoryId).isEqualTo(category.id)
        Assertions.assertThat(response.type).isEqualTo(request.type)
    }

    @Test
    fun updateByEmailNotFoundGroup() {
        val user = userFixture.create()
        val request =
            CategoryNotificationUpdateRequest(
                groupId = 1L,
                categoryId = 1L,
                type = CategoryNotificationType.NONE,
            )

        Assertions
            .assertThatThrownBy {
                categoryNotificationService.update(user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateByEmailNotFoundGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        val category = categoryFixture.create(group = group)
        val request =
            CategoryNotificationUpdateRequest(
                groupId = group.id!!,
                categoryId = category.id!!,
                type = CategoryNotificationType.NONE,
            )

        Assertions
            .assertThatThrownBy {
                categoryNotificationService.update(user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateByEmailNotAllowedIfRoleIsNotTopAdmin() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        groupUserFixture.create(group = group, user = user, role = GroupRole.PENDING)
        val category = categoryFixture.create(group = group)
        val request =
            CategoryNotificationUpdateRequest(
                groupId = group.id!!,
                categoryId = category.id!!,
                type = CategoryNotificationType.NONE,
            )

        Assertions
            .assertThatThrownBy {
                categoryNotificationService.update(user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateByEmailNotFoundCategory() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        groupUserFixture.create(group, user)
        val request =
            CategoryNotificationUpdateRequest(
                groupId = group.id!!,
                categoryId = 1L,
                type = CategoryNotificationType.NONE,
            )

        Assertions
            .assertThatThrownBy {
                categoryNotificationService.update(user.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateByGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val groupAdmin = groupUserRepository.findByUserAndGroup(admin, group)!!
        val category = categoryFixture.create(group = group)

        categoryNotificationService.update(group = group, groupUser = groupAdmin, type = CategoryNotificationType.MENTIONS)

        val response = categoryNotificationRepository.findAll()
        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].type).isEqualTo(CategoryNotificationType.MENTIONS)
    }
}
