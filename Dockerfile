# Build stage
FROM maven:3.8.4-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# (Optional but recommended) pre-fetch dependencies for better layer caching
RUN mvn -B -q -DskipTests dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn -B -DskipTests clean package


# Run stage
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the built artifact from builder stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose the port your app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]