# Base Image
FROM openjdk:21-jdk-slim

#  Arbeitsverzeichnis
WORKDIR /app

#  Copy project metadata
COPY pom.xml mvnw ./
COPY .mvn .mvn

#  Vorbereitung
RUN chmod +x mvnw && ./mvnw dependency:go-offline

#  Copy Quellcode
COPY src src
COPY models models
COPY src/main/resources src/main/resources

#  Build
RUN ./mvnw clean package -DskipTests

#  Run
EXPOSE 8080
CMD ["java", "-jar", "target/playground-0.0.1-SNAPSHOT.jar"]
