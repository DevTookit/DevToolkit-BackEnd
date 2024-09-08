FROM gradle:8.10.0-jdk21 as build

WORKDIR /app

# gradlew 관련 파일들을 복사
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle.kts settings.gradle.kts /app/
COPY ./api/src /app/src

# 실행 권한 부여 (gradlew가 실행될 수 있도록)
RUN chmod +x /app/gradlew
RUN chmod +x /app/gradle

# Gradle 빌드 수행
RUN gradle clean build --no-daemon --no-build-cache

FROM openjdk:21-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "api.jar"]
