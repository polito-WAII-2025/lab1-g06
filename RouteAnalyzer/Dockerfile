FROM eclipse-temurin:24-jdk-alpine
LABEL authors="Giovanni Buzzi"

WORKDIR /app

COPY build/libs/RouteAnalyzer-1.0-SNAPSHOT-all.jar app.jar

#CMD["java","-jar","app.jar"]

CMD java -jar app.jar