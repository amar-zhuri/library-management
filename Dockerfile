FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -DskipTests dependency:go-offline

COPY src src
RUN set -eux; \
    ./mvnw -DskipTests package; \
    JAR_FILE="$(ls /app/target/*.jar | grep -v 'original' | head -n 1)"; \
    mv "$JAR_FILE" /app/target/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN addgroup --system app && adduser --system --ingroup app app
COPY --from=build /app/target/app.jar /app/app.jar

EXPOSE 8081
USER app

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
