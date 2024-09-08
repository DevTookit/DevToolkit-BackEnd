FROM gradle:8.10.0-jdk21 as build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts /app/
COPY ./api/src /app/src

RUN gradle clean build --no-daemon

FROM openjdk:21-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "api.jar"]