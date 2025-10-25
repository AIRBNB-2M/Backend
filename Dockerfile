# 첫 번째 스테이지: 빌드 스테이지
FROM eclipse-temurin:17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드와 Gradle 래퍼 복사
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY gradle/wrapper/ gradle/wrapper/

RUN chmod +x ./gradlew

# 소스 코드 복사
COPY .env /app/.env
COPY src src

# 종속성 설치
RUN ./gradlew dependencies --no-daemon
RUN ./gradlew copyOasToSwagger

# 애플리케이션 빌드
RUN ./gradlew build --no-daemon -x test

# 두 번째 스테이지: 실행 스테이지
FROM container-registry.oracle.com/graalvm/jdk:17

# 작업 디렉토리 설정
WORKDIR /app

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/.env .env

# 실행할 JAR 파일 지정
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]