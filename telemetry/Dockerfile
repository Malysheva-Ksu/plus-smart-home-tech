FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY telemetry/collector/pom.xml .
COPY telemetry/pom.xml ../
COPY pom.xml ../../

COPY telemetry/collector/src ./src

RUN apk add --no-cache maven

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

COPY telemetry/collector/src/main/resources/application.yml application.yml

RUN apk add --no-cache curl netcat-openbsd bind-tools

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-Dspring.config.location=file:application.yml"
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:29092
ENV SCHEMA_REGISTRY_URL=http://schema-registry:8081
ENV KAFKA_TOPIC_SENSORS=telemetry.sensors.v1
ENV KAFKA_TOPIC_HUBS=telemetry.hubs.v1

ENTRYPOINT ["java", "-jar", "app.jar"]