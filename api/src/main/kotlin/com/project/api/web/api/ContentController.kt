package com.project.api.web.api

import com.project.api.service.ContentService
import com.project.api.web.dto.request.ContentCreateRequest
import com.project.api.web.dto.request.ContentFileCreateRequest
import com.project.api.web.dto.request.ContentFileCreateResponse
import com.project.api.web.dto.request.ContentUpdateRequest
import com.project.api.web.dto.response.ContentCreateResponse
import com.project.api.web.dto.response.ContentFolderResponse
import com.project.api.web.dto.response.ContentResponse
import com.project.api.web.dto.response.ContentSearchResponse
import com.project.api.web.dto.response.ContentUpdateResponse
import com.project.api.web.dto.response.HotContentResponse
import com.project.core.internal.ContentType
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.core.annotations.ParameterObject
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
@RequestMapping("/v1/contents")
class ContentController(
    private val contentService: ContentService,
) {
    @GetMapping
    @Operation(summary = "컨텐츠 검색(코드, 게시판형, 폴더)")
    fun readAll(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupId: Long?,
        @RequestParam sectionId: Long?,
        @RequestParam name: String?,
        @RequestParam languages: List<String>?,
        @RequestParam skills: List<String>?,
        @RequestParam writer: String?,
        @RequestParam startDate: Long?,
        @RequestParam endDate: Long?,
        @RequestParam type: ContentType?,
        @ParameterObject pageable: Pageable,
    ): List<ContentSearchResponse> =
        contentService.readAll(
            email = jwt.subject,
            groupId = groupId,
            sectionId = sectionId,
            name = name,
            languages = languages,
            skills = skills,
            writer = writer,
            startDate = startDate,
            endDate = endDate,
            pageable = pageable,
            type = type,
        )

    @GetMapping("/{groupId}/{sectionId}/{contentId}")
    @Operation(summary = "해당 컨텐츠 읽기(코드, 게시판형, 파일)", description = "폴더유형탭의 경우 폴더+파일 list로 나와야해서 api분리")
    fun read(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @PathVariable sectionId: Long,
        @PathVariable contentId: Long,
    ): ContentResponse =
        contentService.read(
            email = jwt.subject,
            groupId = groupId,
            sectionId = sectionId,
            contentId = contentId,
        )

    @GetMapping("/folders/{groupId}/{sectionId}/{folderId}")
    @Operation(summary = "폴더유형탭 파일안에 폴더+ 파일 읽기")
    fun readFolders(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @PathVariable sectionId: Long,
        @PathVariable folderId: Long,
        @ParameterObject pageable: Pageable,
    ): ContentFolderResponse =
        contentService.readFolders(
            email = jwt.subject,
            groupId = groupId,
            sectionId = sectionId,
            folderId = folderId,
            pageable = pageable,
        )

    @PostMapping(path = ["{groupId}/{sectionId}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "컨텐츠 생성(코드, 게시판형, 폴더)")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @PathVariable sectionId: Long,
        @RequestPart(name = "ContentCreateRequest") request: ContentCreateRequest,
        @RequestPart(required = false) files: List<MultipartFile>?,
    ): ContentCreateResponse =
        contentService.create(
            email = jwt.subject,
            groupId = groupId,
            sectionId = sectionId,
            request = request,
            files = files,
        )

    @PostMapping(path = ["/folders/{groupId}/{sectionId}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "컨텐츠 생성(파일)")
    fun createFile(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @PathVariable sectionId: Long,
        @RequestPart(name = "ContentFileCreateRequest") request: ContentFileCreateRequest,
        @RequestPart(required = true) files: List<MultipartFile>,
    ): List<ContentFileCreateResponse> =
        contentService.createFile(
            email = jwt.subject,
            groupId = groupId,
            sectionId = sectionId,
            request = request,
            files = files,
        )

    @PatchMapping(path = ["/{groupId}/{sectionId}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "컨텐츠 수정(코드, 게시판형, 폴더(이름만), 파일(이름만))")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestPart(value = "ContentUpdateRequest") request: ContentUpdateRequest,
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

    @DeleteMapping("/{groupId}/{sectionId}")
    @Operation(summary = "컨텐츠 삭제")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: Long,
        @PathVariable sectionId: Long,
        @RequestParam contentId: Long,
    ): ResponseEntity<Unit> {
        contentService.delete(email = jwt.subject, groupId, sectionId = sectionId, contentId = contentId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/hot")
    @Operation(summary = "핫 게시글")
    fun readHots(): List<HotContentResponse> = contentService.readHots()
}
