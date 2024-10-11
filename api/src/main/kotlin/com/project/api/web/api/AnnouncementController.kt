package com.project.api.web.api

import com.project.api.service.AnnouncementService
import com.project.api.web.dto.request.AnnounceCreateRequest
import com.project.api.web.dto.request.AnnounceUpdateRequest
import com.project.api.web.dto.response.AnnounceCreateResponse
import com.project.api.web.dto.response.AnnounceUpdateResponse
import com.project.api.web.dto.response.AnnouncementResponse
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/announcements/{groupId}")
class AnnouncementController(
    private val announcementService: AnnouncementService,
) {
    @GetMapping
    @Operation(summary = "공지사항 가져오기")
    fun readAll(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @ParameterObject pageable: Pageable,
    ): List<AnnouncementResponse> =
        announcementService.readAll(
            groupId = groupId,
            pageable = pageable,
            email = jwt.subject,
        )

    @GetMapping
    @Operation(summary = "해당 공지사항 읽기")
    fun read(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @RequestParam announceId: Long,
    ): AnnouncementResponse =
        announcementService.read(
            groupId = groupId,
            announceId = announceId,
            email = jwt.subject,
        )

    @PostMapping
    @Operation(summary = "공지사항 생성")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @RequestBody request: AnnounceCreateRequest,
    ): AnnounceCreateResponse =
        announcementService.create(
            email = jwt.subject,
            groupId = groupId,
            request = request,
        )

    @PatchMapping
    @Operation(summary = "공지사항 수정")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @RequestBody request: AnnounceUpdateRequest,
    ): AnnounceUpdateResponse =
        announcementService.update(
            email = jwt.subject,
            groupId = groupId,
            request = request,
        )

    @DeleteMapping
    @Operation(summary = "공지사항 삭제")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @RequestParam announceId: Long,
    ): ResponseEntity<Unit> {
        announcementService.delete(
            groupId = groupId,
            announceId = announceId,
            email = jwt.subject,
        )

        return ResponseEntity.noContent().build()
    }
}
