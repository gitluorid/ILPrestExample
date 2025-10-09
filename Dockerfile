#
# Build stage
#
# Use an official Maven image as the base image
FROM maven:3.9.9-amazoncorretto-21-debian AS build
# Set the working directory in the container
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src
# Build the application using Maven
RUN mvn clean package -DskipTests
# Use an official OpenJDK image as the base image
FROM openjdk:21
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/ilp_submission_1*.jar app.jar
EXPOSE 8080

# Showing how ENV variables can be set
# ENV ILP_SERVICE_URL=https://ilp-rest-2024.azurewebsites.net/

# Set the command to run the application
CMD ["java", "-jar", "./app.jar"]
