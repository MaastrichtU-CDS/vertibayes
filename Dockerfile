FROM openjdk:15-alpine

ARG JAR_FILE=vertibayes-1.0-SNAPSHOT.jar

RUN mkdir /app
COPY $PWD/target/$JAR_FILE /app/app.jar

WORKDIR /app

CMD java -jar ./app.jar