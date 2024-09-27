package com.project.api.web.api

import com.project.api.service.BookmarkService
import com.project.api.web.dto.request.BookmarkCreateRequest
import com.project.core.internal.BookmarkType
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/bookmarks")
class BookmarkController(
    private val bookmarkService: BookmarkService,
) {
    @GetMapping
    @Operation(summary = "북마크 읽기")
    fun readAll(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupId: Long,
        @RequestParam type: BookmarkType?,
        @ParameterObject pageable: Pageable,
    ) = bookmarkService.readAll(
        email = jwt.subject,
        groupId = groupId,
        type = type,
        pageable = pageable,
    )

    @PostMapping
    @Operation(summary = "북마크 생성")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: BookmarkCreateRequest,
    ) = bookmarkService.create(
        email = jwt.subject,
        request = request,
    )

    @DeleteMapping
    @Operation(summary = "북마크 해제(삭제)")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody groupId: Long,
        @RequestParam bookmarkId: Long,
    ): ResponseEntity<Unit> {
        bookmarkService.delete(
            groupId = groupId,
            email = jwt.subject,
            bookmarkId = bookmarkId,
        )
        return ResponseEntity.noContent().build()
    }
}
