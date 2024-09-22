package com.project.api.web.api

import com.project.api.service.CategoryService
import com.project.api.web.dto.request.CategoryCreateRequest
import com.project.api.web.dto.request.CategoryUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
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
@RequestMapping("/v1/categories")
@Tag(name = "Category", description = "Category API")
class CategoryController(
    private val categoryService: CategoryService,
) {
    @GetMapping
    @Operation(summary = "해당 그룹에 카테고리 조회")
    fun readAll(
        @AuthenticationPrincipal jwt: Jwt,
        @ParameterObject pageable: Pageable,
        @RequestParam groupId: Long,
    ) = categoryService.readAll(
        email = jwt.subject,
        pageable = pageable,
        groupId = groupId,
    )

    @PostMapping
    @Operation(summary = "카테고리 생성")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CategoryCreateRequest,
    ) = categoryService.create(jwt.subject, request)

    @PatchMapping
    @Operation(summary = "카테고리 수정")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CategoryUpdateRequest,
    ) = categoryService.update(jwt.subject, request)

    @DeleteMapping
    @Operation(summary = "카테고리 삭제")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam categoryId: Long,
    ): ResponseEntity<Unit> {
        categoryService.delete(jwt.subject, categoryId)
        return ResponseEntity.noContent().build()
    }
}
