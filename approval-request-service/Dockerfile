FROM amazoncorretto:17-alpine
WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Ensure Tomcat + java.io.tmpdir locations exist and stay writable
RUN mkdir -p /tmp/approval-request-service/tmp \
    && mkdir -p /tmp/approval-request-service/tomcat

# REST: 8082
# gRPC는 외부로 오픈할 필요 없음 (Client)
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
