FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S -g 10001 app \
    && adduser -S -D -H -u 10001 -G app app \
    && chown -R app:app /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} ./app.jar

USER app

ENTRYPOINT ["java", "-jar", "./app.jar"]