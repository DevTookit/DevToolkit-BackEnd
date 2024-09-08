package com.project.api.commons.exception

import com.project.api.commons.exception.dto.ErrorResponse
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class RestException : RuntimeException() {
    companion object {
        fun forbidden(
            message: String,
            status: HttpStatus = HttpStatus.FORBIDDEN,
        ): ErrorResponse =
            ErrorResponse(
                message = message,
                status = status,
                timestamp = LocalDateTime.now().toString(),
            )

        fun authorized(
            message: String,
            status: HttpStatus = HttpStatus.UNAUTHORIZED,
        ): ErrorResponse =
            ErrorResponse(
                message = message,
                status = status,
            )

        fun badRequest(
            message: String,
            status: HttpStatus = HttpStatus.BAD_REQUEST,
        ): ErrorResponse =
            ErrorResponse(
                message = message,
                status = status,
            )

        fun notFound(
            message: String,
            status: HttpStatus = HttpStatus.NOT_FOUND,
        ): ErrorResponse =
            ErrorResponse(
                message = message,
                status = status,
            )

        fun conflict(
            message: String,
            status: HttpStatus = HttpStatus.CONFLICT,
        ): ErrorResponse =
            ErrorResponse(
                message = message,
                status = status,
            )
    }
}
