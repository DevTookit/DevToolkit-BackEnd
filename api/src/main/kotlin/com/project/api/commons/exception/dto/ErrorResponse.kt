package com.project.api.commons.exception.dto

import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class ErrorResponse(
    val message: String,
    val timestamp: String = LocalDateTime.now().toString(),
    val status: HttpStatus,
    val path: String? = null,
)
