FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn package -DskipTests


FROM openjdk:21-jre-slim

WORKDIR /app

COPY --from=build /app/target/collector-1.0-SNAPSHOT.jar collector-service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "collector-service.jar"]