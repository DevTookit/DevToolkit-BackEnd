package com.project.api.commons.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.api.commons.exception.RestException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.time.LocalDateTime

@Component
class JwtAuthenticationTokenFilter(
    private val requestMappingHandlerMapping: RequestMappingHandlerMapping,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val handler = requestMappingHandlerMapping.getHandler(request)?.handler as? HandlerMethod

        if (handler != null && handler.methodParameters.any { it.hasParameterAnnotation(AuthenticationPrincipal::class.java) }) {
            val token = request.getHeader(HttpHeaders.AUTHORIZATION)

            if (token == null || !token.startsWith("Bearer ")) {
                RestException
                    .authorized(
                        message = "token is null",
                        status = HttpStatus.UNAUTHORIZED,
                    ).also {
                        response.contentType = "application/json"
                        response.status = it.status.value()
                        response.writer.write(objectMapper.writeValueAsString(it.message))
                    }

                logger.error("Exception 발생 : ${request.requestURI}, token is null, ${response.status}, ${LocalDateTime.now()}")
                return
            }
        }

        filterChain.doFilter(request, response)
    }
}
