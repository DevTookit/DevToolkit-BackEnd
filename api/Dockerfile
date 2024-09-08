FROM gradle:8.10.0-jdk21 as build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts /app/
COPY ./api/src /app/src

# gradle 대신 gradlew 사용, 캐시 없이 빌드 시도
RUN ./gradlew clean build --no-daemon --no-build-cache

FROM openjdk:21-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "api.jar"]
