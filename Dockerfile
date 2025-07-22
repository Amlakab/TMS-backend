# Stage 1: Build the application using Maven and JDK 21
FROM maven:3.9.5-eclipse-temurin-21 AS builder

# Set working directory inside the container
WORKDIR /app

# Copy everything to the container
COPY . .

# Build the application (skipping tests)
RUN mvn clean package -DskipTests

# Stage 2: Run the application with a minimal JDK 21 image
FROM eclipse-temurin:21-jdk-alpine

# Create a working directory
WORKDIR /app

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/usermanagment-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 to the outside world
EXPOSE 8080

# Start the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
