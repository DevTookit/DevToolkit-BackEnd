package com.project.api.commons.exception

import org.springframework.http.HttpStatusCode

class UnAuthorizedException(
    message: String,
    status: HttpStatusCode,
) : RestException(message, status)
