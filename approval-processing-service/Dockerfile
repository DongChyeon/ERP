FROM amazoncorretto:17-alpine
WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# REST: 8083
# gRPC 서버 포트 (예: 9090)
EXPOSE 8083
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
