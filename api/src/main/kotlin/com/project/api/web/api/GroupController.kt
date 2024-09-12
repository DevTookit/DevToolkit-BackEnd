package com.project.api.web.api

import com.project.api.service.GroupService
import com.project.api.web.dto.request.GroupCreateRequest
import com.project.api.web.dto.request.GroupRoleUpdateRequest
import com.project.api.web.dto.request.GroupUpdateRequest
import com.project.api.web.dto.response.GroupResponse
import com.project.api.web.dto.response.GroupRoleUpdateResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
@RequestMapping("/v1/groups")
@Tag(name = "Groups", description = "Group API")
class GroupController(
    private val groupService: GroupService,
) {
    @GetMapping
    @Operation(summary = "각 그룹 조회")
    fun readOne(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupId: Long,
    ): GroupResponse = groupService.readOne(jwt.subject, groupId)

    @PostMapping
    @Operation(summary = "그룹 생성")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: GroupCreateRequest,
    ): GroupResponse = groupService.create(jwt.subject, request)

    @PatchMapping
    @Operation(summary = "그룹 수정")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: GroupUpdateRequest,
    ): GroupResponse = groupService.update(jwt.subject, request)

    @DeleteMapping
    @Operation(summary = "그룹 삭제")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupId: Long,
    ): ResponseEntity<Unit> {
        groupService.delete(jwt.subject, groupId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("role")
    @Operation(summary = "그룹 내 권한 설정", description = "그룹 내 권한설정은 채널방장만 가능하다.")
    fun updateRole(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: GroupRoleUpdateRequest,
    ): GroupRoleUpdateResponse = groupService.updateRole(jwt.subject, request)
}

// 가입요청, 가입요청 승인
