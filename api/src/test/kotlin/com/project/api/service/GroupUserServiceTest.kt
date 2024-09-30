package com.project.api.service

import com.project.api.commons.exception.RestException
import com.project.api.repository.group.GroupUserRepository
import com.project.api.repository.notification.NotificationRepository
import com.project.api.supprot.fixture.GroupFixture
import com.project.api.supprot.fixture.GroupUserFixture
import com.project.api.supprot.fixture.UserFixture
import com.project.api.web.dto.request.GroupUserCreateRequest
import com.project.api.web.dto.request.GroupUserInvitationRequest
import com.project.api.web.dto.request.GroupUserUpdateRequest
import com.project.core.internal.GroupRole
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable

class GroupUserServiceTest(
    @Autowired private val groupUserService: GroupUserService,
    @Autowired private val userFixture: UserFixture,
    @Autowired private val groupFixture: GroupFixture,
    @Autowired private val groupUserFixture: GroupUserFixture,
    @Autowired private val groupUserRepository: GroupUserRepository,
    @Autowired private val notificationRepository: NotificationRepository,
) : TestCommonSetting() {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        notificationRepository.deleteAll()
        groupUserFixture.tearDown()
        groupFixture.tearDown()
        userFixture.tearDown()
    }

    @Test
    fun create() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)

        val joinUser = userFixture.create()
        val request =
            GroupUserCreateRequest(
                groupId = group.id!!,
                name = null,
                role = GroupRole.PENDING,
            )

        val response = groupUserService.create(joinUser.email, request)

        Assertions.assertThat(response.groupId).isEqualTo(group.id)
        Assertions.assertThat(response.role).isEqualTo(GroupRole.PENDING)
    }

    @Test
    fun createNotFoundGroup() {
        val joinUser = userFixture.create()
        val request =
            GroupUserCreateRequest(
                groupId = 1L,
                name = null,
                role = GroupRole.USER,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.create(joinUser.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun createAlreadyGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)

        val joinUser = userFixture.create()
        groupUserFixture.create(group = group, user = joinUser)

        val request =
            GroupUserCreateRequest(
                groupId = group.id!!,
                name = null,
                role = GroupRole.USER,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.create(joinUser.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun createInvitation() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val joinUser = userFixture.create()
        val request =
            GroupUserInvitationRequest(
                groupId = group.id!!,
                userId = joinUser.id!!,
            )

        val response = groupUserService.createInvitation(admin.email, request)

        Assertions.assertThat(response.groupId).isEqualTo(group.id)
        Assertions.assertThat(response.groupName).isEqualTo(group.name)
        Assertions.assertThat(response.role).isEqualTo(GroupRole.INVITED)
    }

    @Test
    fun createInvitationNotFoundGroup() {
        val admin = userFixture.create()
        val joinUser = userFixture.create()
        val request =
            GroupUserInvitationRequest(
                groupId = 1L,
                userId = joinUser.id!!,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.createInvitation(admin.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun createInvitationNotFoundJoinUser() {
        val admin = userFixture.create()
        val request =
            GroupUserInvitationRequest(
                groupId = 1L,
                userId = 100L,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.createInvitation(admin.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readInvitations() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val joinUser = userFixture.create()
        val groupUser = groupUserFixture.create(group = group, user = joinUser, role = GroupRole.INVITED)

        val response = groupUserService.readInvitations(joinUser.email)

        Assertions.assertThat(response).isNotEmpty
        Assertions.assertThat(response[0].groupId).isEqualTo(group.id)
        Assertions.assertThat(response[0].groupUserId).isEqualTo(groupUser.id)
        Assertions.assertThat(response[0].role).isEqualTo(groupUser.role)
    }

    @Test
    fun readInvitationsNotFoundUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)

        Assertions
            .assertThatThrownBy {
                groupUserService.readInvitations("test@kakao.com")
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateInvitation() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val joinUser = userFixture.create()
        val groupUser = groupUserFixture.create(group = group, user = joinUser, role = GroupRole.INVITED)

        groupUserService.acceptInvitation(joinUser.email, groupUser.id!!, true)

        val response = groupUserRepository.findByUserAndGroup(joinUser, group)
        Assertions.assertThat(response!!.id).isEqualTo(groupUser.id)
        Assertions.assertThat(response.role).isEqualTo(GroupRole.USER)
        Assertions.assertThat(response.name).isEqualTo(groupUser.name)
    }

    @Test
    fun updateInvitationNotFoundUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)

        Assertions
            .assertThatThrownBy {
                groupUserService.acceptInvitation("test@kakao.com", 1L, true)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateInvitationNotFoundGroupUser() {
        val admin = userFixture.create()
        val group = groupFixture.create(admin)
        val joinUser = userFixture.create()

        Assertions
            .assertThatThrownBy {
                groupUserService.acceptInvitation(joinUser.email, 100L, true)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteByTopManager() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )

        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.USER,
            )

        groupUserService.delete(admin.email, group.id!!, groupManager.id!!)

        val response = groupUserRepository.findAll()
        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun deleteByManager() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )

        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.USER,
            )

        groupUserService.delete(manager.email, group.id!!, groupUser.id!!)

        val response = groupUserRepository.findAll()
        Assertions.assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun deleteManagerNotFound() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)

        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.USER,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.delete(manager.email, group.id!!, groupUser.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteUserNotFound() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)

        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.delete(manager.email, group.id!!, 100L)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteIfNotSameRole() {
        val admin = userFixture.create()
        val manager1 = userFixture.create()
        val manager2 = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager1 =
            groupUserFixture.create(
                group = group,
                user = manager1,
                role = GroupRole.MANAGER,
            )

        val groupManager2 =
            groupUserFixture.create(
                group = group,
                user = manager2,
                role = GroupRole.MANAGER,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.delete(manager1.email, group.id!!, groupManager2.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteNotAllowedIfRoleIsUser() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )

        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.USER,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.delete(user.email, group.id!!, groupManager.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun deleteNotFoundGroup() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        Assertions
            .assertThatThrownBy {
                groupUserService.delete(user.email, 1L, 2L)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun update() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )
        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.PENDING,
            )

        val request =
            GroupUserUpdateRequest(
                groupId = group.id!!,
                role = GroupRole.USER,
                groupUserId = groupUser.id!!,
            )

        val response = groupUserService.update(manager.email, request)

        Assertions.assertThat(response.groupUserId).isEqualTo(groupUser.id)
        Assertions.assertThat(response.role).isEqualTo(request.role)
        Assertions.assertThat(response.name).isEqualTo(groupUser.name)
    }

    @Test
    fun updateNotFoundGroup() {
        val admin = userFixture.create()
        val request =
            GroupUserUpdateRequest(
                groupId = 1L,
                role = GroupRole.USER,
                groupUserId = 2L,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.update(admin.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateNotFoundAdminOrdManager() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.PENDING,
            )

        val request =
            GroupUserUpdateRequest(
                groupId = group.id!!,
                role = GroupRole.USER,
                groupUserId = groupUser.id!!,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.update(manager.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateUnAuthorizedRoleIsUser() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.USER,
            )

        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.PENDING,
            )

        val request =
            GroupUserUpdateRequest(
                groupId = group.id!!,
                role = GroupRole.USER,
                groupUserId = groupUser.id!!,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.update(manager.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateUnAuthorizedRoleIsNotActive() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.PENDING,
            )

        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.PENDING,
            )

        val request =
            GroupUserUpdateRequest(
                groupId = group.id!!,
                role = GroupRole.USER,
                groupUserId = groupUser.id!!,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.update(manager.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateUnAuthorizedRoleIsEqulaToRequestRole() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )

        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.USER,
            )

        val request =
            GroupUserUpdateRequest(
                groupId = group.id!!,
                role = GroupRole.MANAGER,
                groupUserId = groupUser.id!!,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.update(manager.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun updateNotFoundGroupUser() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )

        val request =
            GroupUserUpdateRequest(
                groupId = group.id!!,
                role = GroupRole.MANAGER,
                groupUserId = 20L,
            )

        Assertions
            .assertThatThrownBy {
                groupUserService.update(manager.email, request)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readRole() {
        val admin = userFixture.create()
        val manager = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )

        val response = groupUserService.readRole(manager.email, group.id!!)

        Assertions.assertThat(response.groupId).isEqualTo(group.id)
        Assertions.assertThat(response.role).isEqualTo(groupManager.role)
        Assertions.assertThat(response.groupUserId).isEqualTo(groupManager.id)
    }

    @Test
    fun readRoleNotFoundGroup() {
        val admin = userFixture.create()
        val manager = userFixture.create()

        Assertions
            .assertThatThrownBy {
                groupUserService.readRole(manager.email, 1L)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readRoleNotFoundGroupUser() {
        val admin = userFixture.create()
        val manager = userFixture.create()

        val group = groupFixture.create(user = admin)

        Assertions
            .assertThatThrownBy {
                groupUserService.readRole(manager.email, group.id!!)
            }.isInstanceOf(RestException::class.java)
    }

    @Test
    fun readAll() {
        val admin = userFixture.create()
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )

        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.USER,
            )

        val response =
            groupUserService.readAll(
                email = admin.email,
                groupId = group.id!!,
                role = null,
                name = null,
                isAccepted = null,
                isApproved = null,
                pageable = Pageable.unpaged(),
            )

        Assertions.assertThat(response.size).isEqualTo(3)
    }

    @Test
    fun readAllByName() {
        val name = "admin"
        val admin = userFixture.create(name = name)
        val manager = userFixture.create()
        val user = userFixture.create()

        val group = groupFixture.create(user = admin)
        val groupManager =
            groupUserFixture.create(
                group = group,
                user = manager,
                role = GroupRole.MANAGER,
            )

        val groupUser =
            groupUserFixture.create(
                group = group,
                user = user,
                role = GroupRole.USER,
            )

        val response =
            groupUserService.readAll(
                email = admin.email,
                groupId = group.id!!,
                role = null,
                name = name,
                isAccepted = null,
                isApproved = null,
                pageable = Pageable.unpaged(),
            )

        Assertions.assertThat(response.size).isEqualTo(1)
        Assertions.assertThat(response.first().name).isEqualTo(admin.name)
        Assertions.assertThat(response.first().role).isEqualTo(GroupRole.TOP_MANAGER)
    }

    @Test
    fun readAllNotFoundGroup() {
        val name = "admin"
        val admin = userFixture.create(name = name)
        val manager = userFixture.create()
        val user = userFixture.create()

        Assertions
            .assertThatThrownBy {
                groupUserService.readAll(
                    email = admin.email,
                    groupId = 1L,
                    role = null,
                    name = name,
                    isAccepted = null,
                    isApproved = null,
                    pageable = Pageable.unpaged(),
                )
            }.isInstanceOf(RestException::class.java)
    }
}
