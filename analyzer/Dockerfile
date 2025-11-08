FROM eclipse-temurin:17-jdk-focal AS builder

WORKDIR /app

COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline || true

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-focal

WORKDIR /app

RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup
USER appuser

COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]