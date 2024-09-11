FROM gradle:8.10.0-jdk21 as build

WORKDIR /app

COPY build.gradle.kts /app/
COPY src /app/src

RUN gradle clean build --no-daemon

FROM openjdk:21-jdk

# deploy 디렉터리를 생성
RUN mkdir -p /deploy

# 작업 디렉터리를 /deploy로 설정
WORKDIR /deploy

ARG JAR_FILE=/build/libs/api-server.jar
# 빌드된 JAR 파일을 복사
COPY ${JAR_FILE} /deploy/api.jar

EXPOSE 8080
# JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/deploy/api.jar"]

