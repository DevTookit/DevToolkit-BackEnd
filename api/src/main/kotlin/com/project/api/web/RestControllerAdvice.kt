package com.project.api.web

import com.project.api.commons.exception.RestException
import com.project.api.commons.exception.dto.ErrorResponse
import com.project.api.internal.ErrorMessage
import com.project.api.service.api.DiscordApi
import com.project.api.service.api.dto.DiscordRequest
import com.project.api.service.api.dto.Embed
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class RestControllerAdvice(
    private val discordApi: DiscordApi,
    private val environment: Environment,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val logFormat = "Exception 발생 : {}, {}, {} ,{}"

    @ExceptionHandler(RestException::class)
    fun handleRestException(
        e: RestException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val date = LocalDateTime.now()
        val status = e.status
        logger.error(logFormat, request.requestURI, e.message, status, date)
        sendMessage(date, request, e, status)
        return ResponseEntity.status(status).body(e.toResponse(request.requestURI))
    }

    @ExceptionHandler(NullPointerException::class)
    fun handleNullPointerException(
        e: NullPointerException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val date = LocalDateTime.now()
        val status = HttpStatus.NOT_FOUND
        logger.error(logFormat, request.requestURI, e.message, status, date)
        sendMessage(date, request, e, status)
        return ResponseEntity
            .status(
                HttpStatus.NOT_FOUND,
            ).body(
                ErrorResponse(
                    message = ErrorMessage.NOT_FOUND.message,
                    status = status,
                    path = request.requestURI,
                ),
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val date = LocalDateTime.now()
        val status = e.statusCode
        logger.error(logFormat, request.requestURI, e.message, status, date)
        sendMessage(date, request, e, status)
        return ResponseEntity
            .status(
                e.statusCode,
            ).body(ErrorResponse(message = e.message, status = status, path = request.requestURI))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        e: RuntimeException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val date = LocalDateTime.now()
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        logger.error(logFormat, request.requestURI, e.message, status, date)
        sendMessage(date, request, e, status)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(message = e.localizedMessage, status = status, path = request.requestURI))
    }

    fun sendMessage(
        date: LocalDateTime,
        request: HttpServletRequest,
        e: Exception,
        status: HttpStatusCode,
    ) {
        if (environment.activeProfiles.contains("stage")) {
            discordApi.sendMessage(
                DiscordRequest(
                    embeds =
                        listOf(
                            Embed.createMessage(
                                date = date,
                                path = request.requestURI,
                                error = e.localizedMessage ?: "No error message",
                                status = status,
                            ),
                        ),
                ),
            )
        }
    }
}
