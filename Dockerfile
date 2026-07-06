FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/phoneWallet-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]