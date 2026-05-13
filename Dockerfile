
# Build Stage
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
# Install necessary libraries for JavaFX/GUI inside Docker
RUN apt-get update && apt-get install -y libgtk-3-0 libglu1-mesa && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/inventoryshoe-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]