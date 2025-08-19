# שלב הבנייה
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# שלב 1: רק קבצי הבנייה (כדי לא לשבור קאש כל הזמן)
COPY pom.xml .
COPY src ./src

# שלב 2: בנייה
RUN mvn clean package -DskipTests

# שלב הריצה
FROM eclipse-temurin:17-jdk
WORKDIR /app
ENV CONFIG_DIR=/app/config

# העתקת ה-JAR
COPY --from=build /app/target/chatbot-calendar-service-0.0.1-SNAPSHOT.jar .

# העתקת הקונפיג בלבד
COPY config/ ${CONFIG_DIR}

# הרצת האפליקציה
CMD ["java", "-jar", "chatbot-calendar-service-0.0.1-SNAPSHOT.jar", "--spring.config.location=file:${CONFIG_DIR}/application.yaml", "-Dlogging.config=${CONFIG_DIR}/logback-spring.xml"]
