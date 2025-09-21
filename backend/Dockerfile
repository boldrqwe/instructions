FROM eclipse-temurin:21-jre AS runtime

ARG JAR_FILE=target/instructions-backend-0.0.1-SNAPSHOT.jar

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
