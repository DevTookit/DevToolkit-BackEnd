package com.project.api.web.api

import com.project.api.service.GroupLogService
import com.project.api.service.GroupService
import com.project.api.web.dto.request.GroupCreateRequest
import com.project.api.web.dto.request.GroupUpdateRequest
import com.project.api.web.dto.response.GroupFileAccessResponse
import com.project.api.web.dto.response.GroupLogResponse
import com.project.api.web.dto.response.GroupResponse
import com.project.api.web.dto.response.HotGroupResponse
import com.project.core.internal.ContentType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/v1/groups")
@Tag(name = "Groups", description = "Group API")
class GroupController(
    private val groupService: GroupService,
    private val groupLogService: GroupLogService,
) {
    @GetMapping("mine")
    @Operation(summary = "내가 생성한 그룹들")
    fun readMine(
        @AuthenticationPrincipal jwt: Jwt,
        @ParameterObject pageable: Pageable,
    ) = groupService.readMine(
        email = jwt.subject,
        pageable = pageable,
    )

    @GetMapping("/me")
    @Operation(summary = "내가 속한 그룹")
    fun readMe(
        @AuthenticationPrincipal jwt: Jwt,
        @ParameterObject pageable: Pageable,
    ): List<GroupResponse> = groupService.readMe(jwt.subject, pageable)

    @GetMapping
    @Operation(summary = "그룹 검색")
    fun readAll(
        @ParameterObject pageable: Pageable,
        @RequestParam name: String?,
    ): Page<GroupResponse> = groupService.readAll(name, pageable)

    @GetMapping("{groupId}")
    @Operation(summary = "각 그룹 조회")
    fun readOne(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
    ): GroupResponse = groupService.readOne(jwt.subject, groupId)

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "그룹 생성")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestPart(name = "GroupCreateRequest") request: GroupCreateRequest,
        @RequestPart(required = false) img: MultipartFile?,
    ): GroupResponse = groupService.create(jwt.subject, request, img)

    @PatchMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "그룹 수정 (그룹을 생성한 유저만이 수정가능하다)")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestPart(name = "GroupUpdateRequest") request: GroupUpdateRequest,
        @RequestPart(required = false) img: MultipartFile?,
    ): GroupResponse = groupService.update(jwt.subject, request, img)

    @DeleteMapping
    @Operation(summary = "그룹 삭제 (그룹을 생성한 유저만이 수정가능하다)")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupId: Long,
    ): ResponseEntity<Unit> {
        groupService.delete(jwt.subject, groupId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("{groupId}/logs")
    @Operation(summary = "그룹내 로그 읽기")
    fun readLogs(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @RequestParam typ: ContentType?,
        @ParameterObject pageable: Pageable,
    ): List<GroupLogResponse> =
        groupLogService.readAll(
            groupId = groupId,
            email = jwt.subject,
            pageable = pageable,
            type = typ,
        )

    @GetMapping("{groupId}/recent")
    @Operation(summary = "최근 연 파일 읽기")
    fun readRecentFiles(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal jwt: Jwt,
        @ParameterObject pageable: Pageable,
    ): List<GroupFileAccessResponse> =
        groupService.readRecentFiles(
            groupId = groupId,
            email = jwt.subject,
            pageable = pageable,
        )

    @GetMapping("hot")
    @Operation(summary = "메인 페이지 핫 그룹 조회")
    fun readHot(): List<HotGroupResponse> = groupService.readHot()
}
