import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
}

group = "com.project"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // domain
    implementation(project(":core"))

    // db
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2")
    // auth
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // spring doc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // aws, s3
    implementation(platform("software.amazon.awssdk:bom:2.27.17"))
    implementation("software.amazon.awssdk:s3")

    // querydsl
    implementation("com.querydsl:querydsl-core:5.1.0")
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")

    //monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // test
    implementation("org.testcontainers:localstack")
    testRuntimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("cloud.localstack:localstack-utils:0.2.20")
    testImplementation("org.testcontainers:junit-jupiter:1.11.3")
    testImplementation("org.testcontainers:testcontainers:1.20.1")
}

// kover 설정
kover {
    reports {
        verify {
            rule {
                groupBy = GroupingEntityType.CLASS
                minBound(80, CoverageUnit.LINE)
                minBound(80, CoverageUnit.INSTRUCTION)
            }
        }

        filters {
            excludes {
                classes(
                    "*MailService",
                    "com.project.api.service.UserService*",
                    "*AuthService",
                    "*FileService",
                    "*S3Service",
                    "*RedisService",
                )
            }
            includes {
                classes("*Service*", "GroupUserService")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
tasks.named("test") {
    finalizedBy("koverVerify")
    doLast {
        if (state.failure != null) {
            throw GradleException("Code coverage verification failed!")
        }
    }
}

val jarName = "api-server.jar"

tasks.named<BootJar>("bootJar") {
    archiveFileName.set(jarName)

    doLast {
        copy {
            from("build/libs/$jarName")
            into("../build/libs")
        }
    }
}
