FROM openjdk:21-jdk
RUN mkdir -p deploy
WORKDIR /deploy
COPY ./build/libs/api-0.0.1.jar api.jar
ENTRYPOINT ["java", "-jar", "/deploy/api.jar"]