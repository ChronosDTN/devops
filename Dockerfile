# ==========================================================================================
# DOCKERFILE MULTI-STAGE PARA A API JAVA SPRING BOOT (CHRONOS DTN)
# ==========================================================================================

FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /usr/src/app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxRAMPercentage=75.0"

RUN addgroup -S dtn_group
RUN adduser -S dtn_user -G dtn_group

WORKDIR /app

COPY --from=builder /usr/src/app/target/*.jar app.jar

RUN chown dtn_user:dtn_group app.jar

EXPOSE 8080

USER dtn_user

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]