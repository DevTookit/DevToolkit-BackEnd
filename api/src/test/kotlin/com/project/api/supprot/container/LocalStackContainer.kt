package com.project.api.supprot.container

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName

@Configuration
@Profile("test")
class LocalStackContainer {
    private val S3_IMAGE = "localstack/localstack"

    @Bean
    fun localStack(): LocalStackContainer =
        LocalStackContainer(DockerImageName.parse(S3_IMAGE))
            .withServices(LocalStackContainer.Service.S3)
}
