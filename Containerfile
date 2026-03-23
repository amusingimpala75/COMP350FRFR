FROM eclipse-temurin:21.0.10_7-jre
WORKDIR /backend
COPY backend/build/libs/backend-1.0-SNAPSHOT-all.jar app.jar
EXPOSE 7070
CMD ["java", "-jar", "app.jar"]
