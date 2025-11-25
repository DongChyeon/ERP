FROM amazoncorretto:17-alpine
WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# REST: 8082
# gRPC는 외부로 오픈할 필요 없음 (Client)
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
