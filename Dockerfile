# Build stage
FROM gradle:7.6.1-jdk17 AS build

# Set the working directory
WORKDIR /project

# Copy the project into the container at /home/gradle/project
COPY . /project

# Build the project and produce an executable jar
RUN gradle build -x test --no-daemon --no-build-cache

# Runtime stage
FROM openjdk:17

WORKDIR /app

EXPOSE 8080

ARG JAR_FILE=/project/build/libs/*.jar
COPY --from=build ${JAR_FILE} app.jar

ARG FIREBASE_KEY=/project/src/main/resources/firebase/firebase-key.json
COPY --from=build ${FIREBASE_KEY} firebase-key.json

ENTRYPOINT ["java","-jar","app.jar"]
