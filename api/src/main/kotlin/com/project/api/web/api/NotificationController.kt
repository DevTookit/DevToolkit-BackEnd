package com.project.api.web.api

import com.project.api.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {
    @GetMapping
    @Operation(summary = "모든 알림 읽기")
    fun readAll(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam isRead: Boolean?,
        @ParameterObject pageable: Pageable,
    ) = notificationService.readAll(
        email = jwt.subject,
        isRead = isRead,
        pageable = pageable,
    )

    @PatchMapping
    @Operation(summary = "알림 읽음으로 변경")
    fun update(
        @RequestParam notificationId: Long,
    ) = notificationService.update(notificationId)
}
