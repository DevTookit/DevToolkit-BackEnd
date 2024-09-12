package com.project.api.commons.exception

import com.project.api.commons.exception.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class RestException(
    override val message: String,
    val status: HttpStatusCode,
) : RuntimeException() {
    fun toResponse(path: String): ErrorResponse =
        ErrorResponse(
            message = this.message,
            status = this.status,
            path = path,
        )

    companion object {
        fun forbidden(
            message: String,
            status: HttpStatusCode = HttpStatus.FORBIDDEN,
        ): ForbiddenException =
            ForbiddenException(
                message = message,
                status = status,
            )

        fun authorized(
            message: String,
            status: HttpStatusCode = HttpStatus.UNAUTHORIZED,
        ): UnAuthorizedException =
            UnAuthorizedException(
                message = message,
                status = status,
            )

        fun badRequest(
            message: String,
            status: HttpStatusCode = HttpStatus.BAD_REQUEST,
        ): BadRequestException =
            BadRequestException(
                message = message,
                status = status,
            )

        fun notFound(
            message: String,
            status: HttpStatusCode = HttpStatus.NOT_FOUND,
        ): NotFoundException =
            NotFoundException(
                message = message,
                status = status,
            )

        fun conflict(
            message: String,
            status: HttpStatusCode = HttpStatus.CONFLICT,
        ): ConflictException =
            ConflictException(
                message = message,
                status = status,
            )
    }
}
