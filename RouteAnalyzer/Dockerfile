FROM eclipse-temurin:24-jdk-alpine
LABEL authors="Giovanni Buzzi"

WORKDIR /app

COPY build/libs/RouteAnalyzer-1.0-SNAPSHOT-all.jar app.jar
COPY evaluation/custom-parameters.yml ./
COPY evaluation/ /app/evaluation/

CMD ["java", "-jar", "app.jar"]