package com.project.api.supprot.container

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class RedisTestContainer : BeforeAllCallback {
    private val REDIS_IMAGE = "redis:7.0.8-alpine"
    private val REDIS_PORT = 6379

    override fun beforeAll(p0: ExtensionContext?) {
        val redis =
            GenericContainer(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(REDIS_PORT)
        redis.start()
        System.setProperty("redis.host", redis.getHost())
        System.setProperty("redis.port", redis.getMappedPort(REDIS_PORT).toString())
    }
}
