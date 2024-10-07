package com.project.api.web.api

import com.project.api.service.GroupUserService
import com.project.api.web.dto.request.GroupUserCreateRequest
import com.project.api.web.dto.request.GroupUserInvitationRequest
import com.project.api.web.dto.request.GroupUserUpdateRequest
import com.project.api.web.dto.response.GroupRoleResponse
import com.project.api.web.dto.response.GroupUserCreateResponse
import com.project.api.web.dto.response.GroupUserResponse
import com.project.core.internal.GroupRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/group-users")
@Tag(name = "Group Users", description = "Group User API")
class GroupUserController(
    private val groupUserService: GroupUserService,
) {
    @PostMapping("join")
    @Operation(summary = "가입요청(role = Pending)")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: GroupUserCreateRequest,
    ): GroupUserCreateResponse = groupUserService.create(jwt.subject, request)

    @PostMapping("/invitations")
    @Operation(summary = "그룹에 초대하기(role= INVITED)")
    fun createInvitation(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: GroupUserInvitationRequest,
    ) = groupUserService.createInvitation(jwt.subject, request)

    @GetMapping("/invitations")
    @Operation(summary = "그룹 초대 확인")
    fun readInvitations(
        @AuthenticationPrincipal jwt: Jwt,
    ) = groupUserService.readInvitations(jwt.subject)

    @PatchMapping("/invitations")
    @Operation(summary = "그룹 초대 수락 or 거절")
    fun updateInvitation(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupUserId: Long,
        @RequestParam isAccepted: Boolean,
    ): ResponseEntity<Unit> {
        groupUserService.acceptInvitation(jwt.subject, groupUserId, isAccepted)
        return ResponseEntity.accepted().build()
    }

    @PatchMapping("role/update")
    @Operation(summary = "가입요청 승인 및 회원 등급 수정 및 정지처분")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: GroupUserUpdateRequest,
    ): GroupUserResponse = groupUserService.update(jwt.subject, request)

    @DeleteMapping
    @Operation(summary = "그룹 멤버 삭제 또는 그룹 멤버방출")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupId: Long,
        @RequestParam groupUserId: Long,
    ): ResponseEntity<Unit> {
        groupUserService.delete(jwt.subject, groupId, groupUserId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("withdraw")
    @Operation(summary = "그룹 탈퇴")
    fun deleteMe(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupId: Long,
    ): ResponseEntity<Unit> {
        groupUserService.deleteMe(jwt.subject, groupId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("role/me")
    @Operation(summary = "해당 그룹내 내 role 확인")
    fun readMyRole(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupId: Long,
    ): GroupRoleResponse = groupUserService.readRole(jwt.subject, groupId)

    @GetMapping
    @Operation(summary = "그룹 내 회원들 읽기")
    fun readAll(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupId: Long,
        @RequestParam role: GroupRole?,
        @RequestParam name: String?,
        @RequestParam isAccepted: Boolean?,
        @RequestParam isApproved: Boolean?,
        @ParameterObject pageable: Pageable,
    ): Page<GroupUserResponse> =
        groupUserService.readAll(
            email = jwt.subject,
            groupId = groupId,
            role = role,
            name = name,
            isAccepted = isAccepted,
            isApproved = isApproved,
            pageable = pageable,
        )
}
