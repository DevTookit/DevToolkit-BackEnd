import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

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

    // domain
    implementation(project(":core"))

    // db
    runtimeOnly("com.h2database:h2")

    // auth
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // spring doc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
            includes {
                classes("*Service*")
            }
            excludes {
            }
        }
    }
}

tasks.named("test") {
    finalizedBy("koverVerify")
    doLast {
        if (state.failure != null) {
            throw GradleException("Code coverage verification failed!")
        }
    }
}
