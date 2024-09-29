package com.project.job

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
@EntityScan(basePackages = ["com.project.core.domain"])
class JobApplication

fun main(args: Array<String>) {
    runApplication<JobApplication>(*args)
}
