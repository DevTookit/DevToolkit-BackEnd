import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
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

repositories {
    mavenCentral()
}

dependencies {
    // spatial
    implementation("org.hibernate:hibernate-spatial:6.2.2.Final")
    implementation("org.locationtech.jts:jts-core:1.18.2")
    implementation("com.bedatadriven:jackson-datatype-jts:2.4")
}

tasks.withType<BootJar> { enabled = false }

tasks.withType<BootBuildImage> { enabled = false }
