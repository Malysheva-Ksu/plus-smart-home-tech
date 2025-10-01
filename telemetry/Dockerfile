FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

COPY telemetry/ ./telemetry/

RUN mvn -f telemetry/pom.xml clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/telemetry/collector/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]