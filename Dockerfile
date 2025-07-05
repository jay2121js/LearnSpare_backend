# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Copy application.properties into the runtime image
COPY src/main/resources/application.properties /app/application.properties

# Expose the app port
EXPOSE 8080

# Run the app with external properties file
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.properties"]
