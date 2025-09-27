FROM eclipse-temurin:17

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew copyOasToSwagger
RUN ./gradlew build -x test

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "build/libs/airbnb-clone-project-0.0.1-SNAPSHOT.jar"]