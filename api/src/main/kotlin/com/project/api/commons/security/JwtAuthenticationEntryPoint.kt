package com.project.api.commons.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.api.commons.exception.RestException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
    private val objectMapper = ObjectMapper()

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        RestException
            .authorized(
                message = authException.message.toString(),
                status = HttpStatus.UNAUTHORIZED,
            ).also {
                response.contentType = "application/json"
                response.status = it.status.value()
                response.writer.write(objectMapper.writeValueAsString(it))
            }
    }
}
