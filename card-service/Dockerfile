FROM eclipse-temurin:21-jre
WORKDIR /app
COPY card-service/target/card-service-0.1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
