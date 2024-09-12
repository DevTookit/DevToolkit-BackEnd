package com.project.api.commons.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.api.commons.exception.RestException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class JwtAccessDeniedHandler : AccessDeniedHandler {
    private val objectMapper = ObjectMapper()

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        RestException
            .forbidden(
                message = accessDeniedException.message.toString(),
            ).also {
                response.contentType = "application/json"
                response.status = it.status.value()
                response.writer.println(objectMapper.writeValueAsString(it))
            }
    }
}
