FROM openjdk:21-jdk

# deploy 디렉터리를 생성
RUN mkdir -p /deploy

# 작업 디렉터리를 /deploy로 설정
WORKDIR /deploy

ARG JAR_FILE=build/libs/*.jar
# 빌드된 JAR 파일을 복사
COPY ${JAR_FILE} /depoloy/api.jar

# JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/deploy/api.jar"]
