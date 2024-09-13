# 1단계: 빌드
FROM gradle:8.10.0-jdk21 as build

WORKDIR /app

# build.gradle.kts 및 소스 코드 복사
COPY build.gradle.kts /app/
COPY src /app/src

# resources 폴더 복사 (security 폴더 포함)
COPY src/main/resources /app/resources

# 빌드 수행
RUN gradle clean build --no-daemon

# 2단계: 실행 환경 설정
FROM openjdk:21-jdk

# deploy 디렉터리를 생성
RUN mkdir -p /deploy

# 작업 디렉터리를 /deploy로 설정
WORKDIR /deploy

# 빌드된 JAR 파일을 복사
ARG JAR_FILE=/build/libs/api-server.jar
COPY ${JAR_FILE} /deploy/api.jar

# resources 폴더를 복사 (application.yml 및 기타 설정 포함)
COPY --from=build /app/resources /deploy/resources

# 프로파일 값을 환경 변수로 설정
ARG SPRING_PROFILE
ENV SPRING_PROFILE=${SPRING_PROFILE}

# 포트 노출
EXPOSE 8080

# JAR 파일 실행 시 프로파일 적용
ENTRYPOINT ["java", "-jar", "/deploy/api.jar", "--spring.profiles.active=${SPRING_PROFILE}"]

