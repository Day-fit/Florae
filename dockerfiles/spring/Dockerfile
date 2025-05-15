FROM maven:3.9.9-eclipse-temurin-21-alpine

COPY ./src app/src/
COPY ./pom.xml app/

WORKDIR /app

## Compile project
RUN mvn clean package -DskipTests

## Relocate and Rename the Jar-file
RUN mv /app/target/Florae*.jar Florae.jar

## Run the jar-file
CMD sh -c 'java -jar Florae.jar'