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

# Stage 2: Create the runtime image with OpenTelemetry Java Agent
FROM eclipse-temurin:21-jdk AS runtime

# 1) Agent 버전을 인자로 받을 수 있게
ARG OTEL_AGENT_VERSION=2.16.0

# 2) Agent 다운로드
RUN mkdir -p /opt/otel \
 && curl -sSL \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar \
    -o /opt/otel/opentelemetry-javaagent.jar

WORKDIR /app

# 3) 빌드된 JAR 복사
COPY --from=builder /home/app/build/libs/*.jar app.jar

EXPOSE 8080

# 4) -javaagent 옵션 추가
ENTRYPOINT ["java", \
            "-javaagent:/opt/otel/opentelemetry-javaagent.jar", \
            "-jar", "app.jar"]
