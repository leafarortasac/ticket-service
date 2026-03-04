FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY shared-contracts /app/shared-contracts
WORKDIR /app/shared-contracts
RUN mvn clean install -DskipTests

WORKDIR /app/ticket-service
COPY ticket-service/pom.xml .
RUN mvn dependency:go-offline

COPY ticket-service/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/ticket-service/target/*.jar app.jar

RUN apk add --no-cache tzdata
ENV TZ=America/Manaus

EXPOSE 8081

ENTRYPOINT ["java", "-Xmx512m", "-Duser.timezone=America/Manaus", "-jar", "app.jar"]