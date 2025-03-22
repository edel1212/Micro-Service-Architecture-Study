# Use an official Java runtime as a parent image
# ☠️ 현재 openjdk:17 버전 이미지에서 anyController null 에러 이슈가 있음  -> 21 버전 사용
FROM openjdk:21-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the compiled jar file into the container
COPY ./build/libs/ecoomerce-0.0.1-SNAPSHOT.jar app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8761

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]