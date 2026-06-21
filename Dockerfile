FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app \
    && chown -R app:app /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} ./app.jar

USER app

ENTRYPOINT ["java", "-jar", "./app.jar"]