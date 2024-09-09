package com.project.api.web

import com.project.api.commons.exception.RestException
import com.project.api.commons.exception.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class RestControllerAdvice {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val logFormat = "Exception 발생 : {}, {} ,{}"

    @ExceptionHandler(RestException::class)
    fun handleRestException(
        e: RestException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(logFormat, e.message, e.status, LocalDateTime.now())
        return ResponseEntity.status(e.status).body(e.toResponse(request.requestURI))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(logFormat, e.message, e.statusCode, LocalDateTime.now())
        return ResponseEntity
            .status(
                e.statusCode,
            ).body(ErrorResponse(message = e.message, status = e.statusCode, path = request.requestURI))
    }
}
