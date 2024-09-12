package com.project.api.commons.exception.dto

import org.springframework.http.HttpStatusCode
import java.time.LocalDateTime

data class ErrorResponse(
    val message: String,
    val timestamp: String = LocalDateTime.now().toString(),
    val status: HttpStatusCode,
    val path: String? = null,
)
