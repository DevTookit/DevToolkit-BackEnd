package com.project.api.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cloud-front")
data class CloudFrontProperties(
    val url: String,
)
