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
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // domain
    implementation(project(":core"))

    // db
    runtimeOnly("com.mysql:mysql-connector-j")

    // auth
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // spring doc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // spatial
    implementation("org.hibernate:hibernate-spatial:6.2.2.Final")
    implementation("org.locationtech.jts:jts-core:1.18.2")
    implementation("com.bedatadriven:jackson-datatype-jts:2.4")

    // test
    testRuntimeOnly("com.h2database:h2")
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
                minBound(0, CoverageUnit.LINE)
                minBound(0, CoverageUnit.INSTRUCTION)
            }
        }

        filters {
            includes {
                classes("*Service*")
            }
            excludes {
                classes("*MailService", "*UserService", "*AuthService")
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
