package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.fixture.CategoryFixture
import com.project.api.fixture.GroupFixture
import com.project.api.fixture.GroupUserFixture
import com.project.api.fixture.UserFixture
import com.project.api.repository.category.CategoryNotificationRepository
import com.project.api.repository.category.CategoryRepository
import com.project.api.web.dto.request.CategoryCreateRequest
import com.project.api.web.dto.request.CategoryUpdateRequest
import com.project.core.internal.CategoryNotificationType
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
class CategoryServiceTest(
    @Autowired private val categoryFixture: CategoryFixture,
    @Autowired private val categoryService: CategoryService,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val categoryNotificationRepository: CategoryNotificationRepository,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val categoryRepository: CategoryRepository,
    @Autowired private val groupUserFixture: GroupUserFixture,
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
    fun create() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val request =
            CategoryCreateRequest(
                name = "category",
                groupId = group.id!!,
                isPublic = true,
            )

        val response =
            categoryService.create(
                email = user.email,
                request = request,
            )

        val notificationResponse = categoryNotificationRepository.findAll()

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.groupId).isEqualTo(group.id)
        Assertions.assertThat(response.isPublic).isEqualTo(request.isPublic)
        Assertions.assertThat(notificationResponse).isNotEmpty
        Assertions.assertThat(notificationResponse[0].type).isEqualTo(CategoryNotificationType.ALL)
    }

    @Test
    fun createNotFoundGroup() {
        val user = userFixture.create()
        val request =
            CategoryCreateRequest(
                name = "category",
                groupId = 1L,
                isPublic = true,
            )

        Assertions
            .assertThatThrownBy {
                categoryService.create(
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
            CategoryCreateRequest(
                name = "category",
                groupId = group.id!!,
                isPublic = true,
            )

        Assertions
            .assertThatThrownBy {
                categoryService.create(
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
            CategoryCreateRequest(
                name = "category",
                groupId = group.id!!,
                isPublic = true,
            )

        Assertions
            .assertThatThrownBy {
                categoryService.create(email = user.email, request = request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun update() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val category = categoryFixture.create(group = group)
        val request =
            CategoryUpdateRequest(
                name = "category",
                groupId = group.id!!,
                isPublic = true,
                categoryId = category.id!!,
            )

        val response = categoryService.update(email = user.email, request = request)

        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.groupId).isEqualTo(group.id)
        Assertions.assertThat(response.isPublic).isEqualTo(request.isPublic)
        Assertions.assertThat(response.categoryId).isEqualTo(category.id)
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
                categoryService.update(email = user.email, request = request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateNotFoundGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        val category = categoryFixture.create(group = group)
        val request =
            CategoryUpdateRequest(
                name = "category",
                groupId = group.id!!,
                isPublic = true,
                categoryId = category.id!!,
            )

        Assertions
            .assertThatThrownBy {
                categoryService.update(email = user.email, request = request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateNotAllowedIfRoleIsNotTopAdmin() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        groupUserFixture.create(group = group, user = user)
        val category = categoryFixture.create(group = group)
        val request =
            CategoryUpdateRequest(
                name = "category",
                groupId = group.id!!,
                isPublic = true,
                categoryId = category.id!!,
            )

        Assertions
            .assertThatThrownBy {
                categoryService.update(email = user.email, request = request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateNotFoundCategory() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val request =
            CategoryUpdateRequest(
                name = "category",
                groupId = group.id!!,
                isPublic = true,
                categoryId = 1L,
            )

        Assertions
            .assertThatThrownBy {
                categoryService.update(email = admin.email, request = request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun delete() {
        val user = userFixture.create()
        val group = groupFixture.create(user)
        val category = categoryFixture.create(group = group)

        categoryService.delete(email = user.email, categoryId = category.id!!)

        val categoryResponse = categoryRepository.findAll()
        val notificationResponse = categoryNotificationRepository.findAll()

        Assertions.assertThat(categoryResponse).isEmpty()
        Assertions.assertThat(notificationResponse).isEmpty()
    }

    @Test
    fun deleteNotFoundCategory() {
        val user = userFixture.create()
        val group = groupFixture.create(user)

        Assertions
            .assertThatThrownBy {
                categoryService.delete(email = user.email, categoryId = 1L)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteNotFoundGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        val category = categoryFixture.create(group = group)

        Assertions
            .assertThatThrownBy {
                categoryService.delete(email = user.email, categoryId = category.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteNotAllowedIfRoleIsNotTopAdmin() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val user = userFixture.create()
        groupUserFixture.create(group = group, user = user)
        val category = categoryFixture.create(group = group)

        Assertions
            .assertThatThrownBy {
                categoryService.delete(email = user.email, categoryId = category.id!!)
            }.isInstanceOf(RestException::class.java)
    }
}
