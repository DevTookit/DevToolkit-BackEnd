package com.project.api.web

import com.project.api.commons.exception.RestException
import com.project.api.commons.exception.dto.ErrorResponse
import com.project.api.internal.ErrorMessage
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class RestControllerAdvice {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val logFormat = "Exception 발생 : {}, {}, {} ,{}"

    @ExceptionHandler(RestException::class)
    fun handleRestException(
        e: RestException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(logFormat, request.requestURI, e.message, e.status, LocalDateTime.now())
        return ResponseEntity.status(e.status).body(e.toResponse(request.requestURI))
    }

    @ExceptionHandler(NullPointerException::class)
    fun handleNullPointerException(
        e: NullPointerException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(logFormat, request.requestURI, e.message, HttpStatus.NOT_FOUND, LocalDateTime.now())
        return ResponseEntity
            .status(
                HttpStatus.NOT_FOUND,
            ).body(
                ErrorResponse(
                    message = ErrorMessage.NOT_FOUND.message,
                    status = HttpStatus.NOT_FOUND,
                    path = request.requestURI,
                ),
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(logFormat, request.requestURI, e.message, e.statusCode, LocalDateTime.now())
        return ResponseEntity
            .status(
                e.statusCode,
            ).body(ErrorResponse(message = e.message, status = e.statusCode, path = request.requestURI))
    }
}
