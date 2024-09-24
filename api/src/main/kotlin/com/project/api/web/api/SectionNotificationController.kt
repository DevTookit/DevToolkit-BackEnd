package com.project.api.web.api

import com.project.api.service.SectionNotificationService
import com.project.api.web.dto.request.SectionNotificationUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/sections-notification")
class SectionNotificationController(
    private val sectionNotificationService: SectionNotificationService,
) {
    @PatchMapping
    @Operation(summary = "카테고리별 알림 설정 수정")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: SectionNotificationUpdateRequest,
    ) = sectionNotificationService.update(jwt.subject, request)
}
