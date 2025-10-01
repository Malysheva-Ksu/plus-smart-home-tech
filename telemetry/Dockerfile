FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

COPY telemetry/ ./telemetry/

RUN mvn -f telemetry/pom.xml clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/telemetry/collector/target/*.jar app.jar

ENTRYPOINT ["java", \
     "-Dspring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS}", \
     "-Dspring.kafka.properties.schema.registry.url=${KAFKA_SCHEMA_REGISTRY_URL}", \
     "-jar", "app.jar"]