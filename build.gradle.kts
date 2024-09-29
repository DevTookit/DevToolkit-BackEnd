plugins {
    id("org.springframework.boot") version "3.3.2" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("org.jetbrains.kotlinx.kover") version "0.8.3" apply false
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0" apply false
    kotlin("plugin.jpa") version "2.0.0" apply false
    kotlin("kapt") version "2.0.0"
}

repositories {
    mavenCentral()
}
subprojects {
    group = "com.project"
    version = "1.0.0"

    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        kapt("org.springframework.boot:spring-boot-configuration-processor")

        // kotlin
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib")

        // Spring Data JPA
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    repositories {
        mavenCentral() // Maven Central 리포지토리를 추가하여 외부 의존성 해결
    }
}

project(":api") {
    // test module specific configurations if necessary
}

project(":core") {
}

project(":job") {
}
