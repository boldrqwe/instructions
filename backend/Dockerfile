# backend/Dockerfile
# syntax=docker/dockerfile:1.7

########### BUILD STAGE ###########
FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /app

# кэш зависимостей
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests dependency:go-offline

# исходники и сборка
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

########## RUNTIME STAGE ##########
FROM eclipse-temurin:25
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

