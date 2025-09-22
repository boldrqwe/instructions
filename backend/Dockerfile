# syntax=docker/dockerfile:1.7

########################
# 1) BUILD STAGE
########################
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Кэш зависимостей
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests dependency:go-offline

# Исходники и сборка
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

########################
# 2) RUNTIME STAGE
########################
FROM eclipse-temurin:21-jre
WORKDIR /app

# Кладём собранный jar из build-стейджа
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
