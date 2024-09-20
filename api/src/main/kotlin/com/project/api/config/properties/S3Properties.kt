package com.project.api.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "s3")
data class S3Properties(
    val accessKey: String,
    val secretKey: String,
    val bucketName: String,
)
