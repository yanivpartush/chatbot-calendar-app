
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app


COPY pom.xml .
COPY src ./src


RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jdk
WORKDIR /app
ENV CONFIG_DIR=/app/config


COPY --from=build /app/target/chatbot-calendar-service-0.0.1-SNAPSHOT.jar .


COPY config/ ${CONFIG_DIR}


CMD ["java", "-jar", "chatbot-calendar-service-0.0.1-SNAPSHOT.jar", "--spring.config.location=file:${CONFIG_DIR}/application.yaml", "-Dlogging.config=${CONFIG_DIR}/logback-spring.xml"]
