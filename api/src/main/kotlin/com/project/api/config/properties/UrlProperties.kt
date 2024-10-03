package com.project.api.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "url")
data class UrlProperties(
    val discord: String,
)
