# Multi-stage build for a small runtime image
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
# Pre-fetch dependencies (best-effort)
RUN mvn -q -DskipTests dependency:go-offline || true

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/kc-backend-0.0.1-SNAPSHOT.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8000
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
