FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY card-service/pom.xml card-service/pom.xml
COPY game-service/pom.xml game-service/pom.xml
COPY realtime-service/pom.xml realtime-service/pom.xml
COPY game-service/src game-service/src
RUN mvn -pl game-service -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/game-service/target/game-service-0.1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
