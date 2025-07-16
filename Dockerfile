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

# Stage 2: Runtime image with OpenTelemetry(Java Agent) for SigNoz Cloud
FROM eclipse-temurin:21-jdk AS runtime

# 변경점: Agent 버전만 ARG 로 받고, region 은 us 로 하드코딩
ARG OTEL_AGENT_VERSION=2.16.0

# OpenTelemetry Java Agent 다운로드
RUN mkdir -p /opt/otel \
 && curl -sSL \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar \
    -o /opt/otel/opentelemetry-javaagent.jar

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /home/app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "\
  java \
    -Duser.timezone=Asia/Seoul \
    -javaagent:/opt/otel/opentelemetry-javaagent.jar \
    -Dotel.exporter.otlp.endpoint=https://ingest.us.signoz.cloud:443 \
    -Dotel.exporter.otlp.headers=\"signoz-ingestion-key=${SIGNOZ_INGESTION_KEY}\" \
    -Dotel.service.name=${OTEL_SERVICE_NAME:-backend} \
    -Dotel.resource.attributes=deployment.environment=${DEPLOY_ENV} \
    -jar app.jar\
"]