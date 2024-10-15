package com.project.api.web.api

import com.project.api.service.CommentService
import com.project.api.web.dto.request.CommentCreateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/comments")
@Tag(name = "Comments", description = "Comment API")
class CommentController(
    private val commentService: CommentService,
) {

    @GetMapping("/{groupId}/{contentId}")
    @Operation(summary = "해당 게시글(혹은 공지사항) 댓글 읽기")
    fun readAll(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId:Long,
        @PathVariable contentId: Long,
    ) {

    }

    @PostMapping
    @Operation(summary = "해당 게시글(혹은 공지사항) 댓글 생성")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CommentCreateRequest,
    ) {

    }

    @PatchMapping("/{groupId}/{contentId}")
    @Operation(summary = "해당 댓글 수정", description = "작성자만 가능하다.")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId:Long,
        @PathVariable contentId: Long,
    ) {

    }

    @DeleteMapping("/{groupId}/{contentId}")
    @Operation(summary = "해당 댓글 삭제 ", description = "작성자 또는 어드민만 삭제가능")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId:Long,
        @PathVariable contentId: Long,
    ) {

    }

}