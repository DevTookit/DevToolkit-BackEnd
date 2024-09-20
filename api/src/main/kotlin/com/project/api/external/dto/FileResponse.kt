package com.project.api.external.dto

data class FileResponse(
    val url: String? = null,
    val size: Long? = null,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
)
