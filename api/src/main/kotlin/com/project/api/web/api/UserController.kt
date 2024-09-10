package com.project.api.web.api

import com.project.api.service.UserService
import com.project.api.web.dto.request.UserCreateRequest
import com.project.api.web.dto.request.UserLoginRequest
import com.project.api.web.dto.request.UserResponse
import com.project.api.web.dto.request.UserUpdateRequest
import com.project.api.web.dto.response.TokenResponse
import com.project.api.web.dto.response.UserLoginResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
@Tag(name = "Users", description = "User API")
class UserController(
    private val userService: UserService,
) {
    @PostMapping("create")
    @Operation(summary = "유저 생성")
    fun create(
        @Valid @RequestBody request: UserCreateRequest,
    ): ResponseEntity<Unit> {
        userService.create(request)
        return ResponseEntity
            .ok()
            .build()
    }

    @PostMapping("login")
    @Operation(summary = "로그인")
    fun login(
        @RequestBody request: UserLoginRequest,
    ): UserLoginResponse = userService.login(request)

    @GetMapping("me")
    @Operation(summary = "내정보 조회")
    fun readMe(
        @AuthenticationPrincipal jwt: Jwt,
    ): UserResponse = userService.readMe(jwt.subject)

    @PatchMapping("update")
    @Operation(summary = "내정보 수정")
    fun updatePassword(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UserUpdateRequest,
    ): UserResponse = userService.update(jwt.subject, request)

    @PostMapping("token")
    @Operation(summary = "토큰 발급")
    fun createToken(
        @AuthenticationPrincipal jwt: Jwt,
    ): TokenResponse = userService.createToken(jwt.subject)
}
