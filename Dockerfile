# Stage 1: Build the application
FROM gradle:8.4-jdk21 AS builder

WORKDIR /home/app

# Gradle 캐시 최적화를 위해 먼저 의존성만 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle build -x test || return 0

# 전체 소스 복사 및 다시 빌드
COPY . .
RUN gradle clean build -x test

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=builder /home/app/build/libs/*.jar app.jar

EXPOSE 8080

# --debug 모드로 로그 상세화
CMD ["java", "-jar", "app.jar", "--debug"]
