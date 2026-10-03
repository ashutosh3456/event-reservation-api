# Use Java 21
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first
# (Docker caches this layer)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the app
RUN ./mvnw package -DskipTests

# Run the jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "target/EventReservationAPI-0.0.1-SNAPSHOT.jar"]