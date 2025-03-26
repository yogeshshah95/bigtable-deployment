# Use official OpenJDK 8 runtime image
FROM openjdk:8-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the built jar from target folder to /app
COPY target/TestingBTLibrary-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

# Copy the Bigtable credentials to the container
COPY src/main/java/cred/gcs_bucket.json /app/cred/gcs_bucket.json