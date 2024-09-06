package com.project.devapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DevApiApplication

fun main(args: Array<String>) {
    runApplication<DevApiApplication>(*args)
}
