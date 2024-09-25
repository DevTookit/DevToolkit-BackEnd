package com.project.api.web.api

import com.project.api.service.ContentService
import com.project.api.web.dto.request.ContentCreateRequest
import com.project.api.web.dto.request.ContentUpdateRequest
import com.project.api.web.dto.response.ContentCreateResponse
import com.project.api.web.dto.response.ContentResponse
import com.project.api.web.dto.response.ContentUpdateResponse
import io.swagger.v3.oas.annotations.Operation
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
@RequestMapping("/v1/contents/{groupId}/{sectionId}")
class ContentController(
    private val contentService: ContentService,
) {
    @GetMapping
    @Operation(summary = "해당 컨텐츠 읽기(코드, 게시판형)")
    fun read(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @PathVariable sectionId: Long,
        @RequestParam contentId: Long,
    ): ContentResponse =
        contentService.read(
            email = jwt.subject,
            groupId = groupId,
            sectionId = sectionId,
            contentId = contentId,
        )

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "컨텐츠 생성(코드, 게시판형)")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @PathVariable sectionId: Long,
        @RequestPart request: ContentCreateRequest,
        @RequestPart(required = false) files: List<MultipartFile>?,
    ): ContentCreateResponse =
        contentService.create(
            email = jwt.subject,
            groupId = groupId,
            sectionId = sectionId,
            request = request,
            files = files,
        )

    @PatchMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "컨텐츠 수정(코드, 게시판형)")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestPart request: ContentUpdateRequest,
        @PathVariable groupId: Long,
        @PathVariable sectionId: Long,
        @RequestPart(required = false) files: List<MultipartFile>?,
    ): ContentUpdateResponse =
        contentService.update(
            email = jwt.subject,
            groupId = groupId,
            sectionId = sectionId,
            request = request,
            files = files,
        )

    @DeleteMapping
    @Operation(summary = "컨텐츠 삭제(코드, 게시판형)")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @PathVariable sectionId: Long,
        @RequestParam contentId: Long,
    ): ResponseEntity<Unit> {
        contentService.delete(email = jwt.subject, groupId, sectionId = sectionId, contentId = contentId)
        return ResponseEntity.noContent().build()
    }
}
