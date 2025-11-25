FROM amazoncorretto:17-alpine
WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# WebSocket/REST 포트
EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
