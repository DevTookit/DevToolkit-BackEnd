package com.project.api.web.api

import com.project.api.service.UserService
import com.project.api.web.dto.request.UserCreateRequest
import com.project.api.web.dto.request.UserLoginRequest
import com.project.api.web.dto.request.UserResetPasswordRequest
import com.project.api.web.dto.request.UserUpdateRequest
import com.project.api.web.dto.response.TokenResponse
import com.project.api.web.dto.response.UserLoginResponse
import com.project.api.web.dto.response.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/v1/users")
@Tag(name = "Users", description = "User API")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("verify-email")
    @Operation(summary = "이메일 인증", description = "response값으로 인증코드 값 보냄")
    fun verifyEmail(
        @RequestParam email: String,
    ): String = userService.verifyEmail(email)

    @PatchMapping("verify-email")
    @Operation(summary = "이메일 인증 성공시", description = "인증코드 일치시 해당 요청보내줘야 로그인 가능")
    fun updateVerifyEmail(
        @RequestParam email: String,
    ): ResponseEntity<Unit> {
        userService.updateVerifyEmail(email)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("create", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "유저 생성")
    fun create(
        @Valid @RequestPart request: UserCreateRequest,
        @RequestPart(required = false) img: MultipartFile?,
    ): ResponseEntity<Unit> {
        userService.create(request, img)
        return ResponseEntity
            .ok()
            .build()
    }

    @PostMapping("login")
    @Operation(summary = "로그인")
    fun login(
        @RequestBody request: UserLoginRequest,
    ): UserLoginResponse = userService.login(request)

    @GetMapping("find-email")
    @Operation(summary = "아이디 찾기")
    fun findEmail(email: String) = userService.findEmail(email)

    @PatchMapping("reset-password")
    @Operation(summary = "비밀번호 찾기(재설정)")
    fun resetPassword(
        @RequestBody request: UserResetPasswordRequest,
    ) = userService.resetPassword(request)

    @GetMapping("me")
    @Operation(summary = "내정보 조회")
    fun readMe(
        @AuthenticationPrincipal jwt: Jwt,
    ): UserResponse = userService.readMe(jwt.subject)

    @PatchMapping("update", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "내정보 수정")
    fun updatePassword(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestPart request: UserUpdateRequest,
        @RequestPart(required = false) img: MultipartFile?,
    ): UserResponse = userService.update(jwt.subject, request, img)

    @PostMapping("token")
    @Operation(summary = "토큰 발급")
    fun createToken(
        @AuthenticationPrincipal jwt: Jwt,
    ): TokenResponse = userService.createToken(jwt.subject)

    @GetMapping("{userId}")
    @Operation(summary = "한 회원 검색")
    fun readOne(
        @PathVariable userId: Long,
    ): UserResponse = userService.readOne(userId)

    @GetMapping("onboarding")
    @Operation(summary = "온보딩 다 했는지 확인")
    fun checkOnBoarding(
        @AuthenticationPrincipal jwt: Jwt,
    ) = userService.checkOnBoarding(jwt.subject)

    @PatchMapping("onboarding")
    @Operation(summary = "온보딩 완료 update(데이터 수정)")
    fun updateOnBoarding(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam isOnBoarding: Boolean,
    ): ResponseEntity<Unit> {
        userService.updateOnBoarding(jwt.subject, isOnBoarding)
        return ResponseEntity
            .accepted()
            .build()
    }
}
