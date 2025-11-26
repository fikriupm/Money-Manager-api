FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/moneymanager-0.0.1-SNAPSHOT.jar moeneymanager-v1.0.app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "moneymanager-v1.0.app.jar"]