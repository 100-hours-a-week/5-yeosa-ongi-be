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

# Stage 2: Runtime image with OpenTelemetry(Java Agent) for SigNoz
FROM eclipse-temurin:21-jdk AS runtime

# Agent 및 SigNoz Collector endpoint를 인자로 받음
ARG OTEL_AGENT_VERSION=2.16.0
ARG SIGNOZ_OTLP_ENDPOINT="http://signoz-collector:4317"

# OpenTelemetry Java Agent 다운로드
RUN mkdir -p /opt/otel \
 && curl -sSL \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar \
    -o /opt/otel/opentelemetry-javaagent.jar

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /home/app/build/libs/*.jar app.jar

EXPOSE 8080

# Java Agent 및 OTLP 설정을 시스템 프로퍼티로 주입
ENTRYPOINT ["sh", "-c", "\
  java \
    -javaagent:/opt/otel/opentelemetry-javaagent.jar \
    -Dotel.exporter.otlp.endpoint=${SIGNOZ_OTLP_ENDPOINT} \
    -Dotel.service.name=${OTEL_SERVICE_NAME:-backend-dev} \
    -Dotel.resource.attributes=deployment.environment=${DEPLOY_ENV:-dev} \
    -jar app.jar\
"]