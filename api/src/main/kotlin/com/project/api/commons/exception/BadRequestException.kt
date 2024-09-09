package com.project.api.commons.exception

import org.springframework.http.HttpStatusCode

class BadRequestException(
    message: String,
    status: HttpStatusCode,
) : RestException(message, status)
