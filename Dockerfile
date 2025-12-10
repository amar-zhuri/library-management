# Stage 1: Build the application
# We use a Maven image with Java 21 to compile your code
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .

# Build the project and skip tests to save time during container build
RUN mvn clean package -DskipTests

# Stage 2: Run the application
# We use a lightweight Java 21 JRE image just to run the jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar from the previous stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]