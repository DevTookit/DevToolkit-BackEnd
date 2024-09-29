package com.project.job.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "redis")
data class RedisProperties(
    val host: String,
    val port: Int,
)
