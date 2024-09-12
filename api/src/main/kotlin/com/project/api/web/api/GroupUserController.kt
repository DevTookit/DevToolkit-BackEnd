package com.project.api.web.api

import com.project.api.service.GroupUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/group-users")
@Tag(name = "Group Users", description = "Group User API")
class GroupUserController(
    private val groupUserService: GroupUserService,
) {
    @DeleteMapping
    @Operation(summary = "그룹 멤버 삭제 또느 그룹 멤버방출")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam groupId: Long,
        @RequestParam groupUserId: Long,
    ): ResponseEntity<Unit> {
        groupUserService.delete(jwt.subject, groupId, groupUserId)
        return ResponseEntity.noContent().build()
    }
}
