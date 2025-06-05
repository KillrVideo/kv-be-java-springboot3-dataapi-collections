# Build stage
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /workspace/app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn

# Copy the project files
COPY pom.xml .
COPY src src

# Build a release artifact
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Set working directory
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /workspace/app/target/*.jar app.jar

# Add wait-for-it script for database readiness check
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

# Create volume for logs
VOLUME /app/logs

# Environment variables
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="-Xmx512m -Xms256m" \
    SPRING_PROFILES_ACTIVE=prod

# Expose the application port
EXPOSE 8443

# Start the application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"] 