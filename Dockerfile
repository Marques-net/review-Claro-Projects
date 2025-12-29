FROM maven:3.8.5-openjdk-17 AS build
ENV TZ="America/Sao_Paulo"

WORKDIR /usr/local/app
COPY . ./
RUN mvn clean install

FROM openjdk:17.0.1-jdk-slim
ENV TZ="America/Sao_Paulo"

WORKDIR /usr/local/app
COPY --from=build /usr/local/app/target/*.jar app.jar

EXPOSE 8080
USER nobody
CMD ["java", "-jar", "app.jar"]