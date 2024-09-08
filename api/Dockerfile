FROM openjdk:21-jdk
RUN mkdir -p deploy
WORKDIR /deploy
COPY ./build/libs/*.jar api.jar
ENTRYPOINT ["java", "-jar", "/deploy/api.jar"]