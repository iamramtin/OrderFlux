FROM amazoncorretto:22-alpine

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and project files
COPY gradlew ./
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Copy the source code
COPY src ./src

# Copy the source code
RUN chmod +x gradlew

# Build the application
RUN ./gradlew build --no-daemon

# Expose the port the app runs on
EXPOSE 8888

# Run the jar file
CMD ["java", "-jar", "build/libs/OrderFlux-0.0.1.jar"]