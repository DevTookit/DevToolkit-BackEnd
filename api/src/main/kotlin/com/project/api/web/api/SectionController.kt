package com.project.api.web.api

import com.project.api.service.SectionService
import com.project.api.web.dto.request.CategoryUpdateRequest
import com.project.api.web.dto.request.SectionCreateRequest
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
@RequestMapping("/v1/sections")
@Tag(name = "Sections", description = "Section API")
class SectionController(
    private val sectionService: SectionService,
) {
    @GetMapping
    @Operation(summary = "해당 그룹의 카테고리(메뉴, 저장소) 조회")
    fun readAll(
        @AuthenticationPrincipal jwt: Jwt,
        @ParameterObject pageable: Pageable,
        @RequestParam groupId: Long,
        @RequestParam parentSectionId: Long?,
    ) = sectionService.readAll(
        email = jwt.subject,
        pageable = pageable,
        groupId = groupId,
        parentSectionId = parentSectionId,
    )

    @PostMapping
    @Operation(summary = "카테고리 생성(그룹 생성자만이 카테고리를 생성가능)")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: SectionCreateRequest,
    ) = sectionService.create(jwt.subject, request)

    @PatchMapping
    @Operation(summary = "카테고리 수정")
    fun updateCategory(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CategoryUpdateRequest,
    ) = sectionService.update(jwt.subject, request)

    @DeleteMapping
    @Operation(summary = "카테고리 삭제")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam sectionId: Long,
    ): ResponseEntity<Unit> {
        sectionService.delete(jwt.subject, sectionId)
        return ResponseEntity.noContent().build()
    }
}
