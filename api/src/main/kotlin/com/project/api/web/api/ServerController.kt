package com.project.api.web.api

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/server")
class ServerController {
    @GetMapping("time")
    @Operation(summary = "서버 시간")
    fun read(): ResponseEntity<Long> = ResponseEntity.ok(System.currentTimeMillis())
}
