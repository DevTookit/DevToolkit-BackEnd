FROM gradle:8.10.0-jdk21 as build

WORKDIR /app

COPY build.gradle.kts /app/

COPY src /app/src

RUN gradle clean build --no-daemon

FROM openjdk:21-jdk

RUN mkdir -p /deploy

WORKDIR /deploy
ARG JAR_FILE=build/libs/api-server.jar
COPY ${JAR_FILE} /deploy/api.jar

# resources 폴더를 복사 (application.yml 및 기타 설정 포함)
COPY --from=build /app/src/main/resources /deploy/resources

ARG SPRING_PROFILE
# 프로파일 값을 환경 변수로 설정
ENV SPRING_PROFILE=${SPRING_PROFILE}

EXPOSE 8080
# JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/deploy/api.jar", "--spring.profiles.active=${SPRING_PROFILE}"]

