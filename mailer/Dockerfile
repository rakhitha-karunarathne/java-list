# syntax=docker/dockerfile:1
# App-only image (no database). Point at MariaDB with SPRING_DATASOURCE_*.
#
# Build (from repo root):
#   docker build -t ghcr.io/<owner>/java-list:latest -f mailer/Dockerfile mailer
#
# Run (example):
#   docker run --rm -p 8080:8080 \
#     -e SPRING_DATASOURCE_URL=jdbc:mariadb://db.example:3306/mailer \
#     -e SPRING_DATASOURCE_USERNAME=mailer \
#     -e SPRING_DATASOURCE_PASSWORD=secret \
#     -e MAILER_JWT_SECRET='at-least-32-bytes-of-secret-here!!' \
#     -e MAILER_OPAQUE_TOKEN_SECRET='at-least-32-bytes-opaque-secret!' \
#     -e MAILER_SECRETS_KEY_1='at-least-32-bytes-secrets-key!!!!' \
#     -e MAILER_PUBLIC_BASE_URL=https://lists.example.com \
#     ghcr.io/<owner>/java-list:latest
# Optional rotate: also set MAILER_SECRETS_KEY_2 and MAILER_SECRETS_MIGRATE_ON_STARTUP=true.

FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY frontend ./frontend
COPY src ./src
# Repackaged Boot jar only (ignore *.jar.original if present).
RUN mvn -q -DskipTests package \
  && JAR="$(ls -1 /workspace/target/mailer-*.jar | grep -v '\.original$' | head -n1)" \
  && test -n "$JAR" \
  && mv "$JAR" /workspace/target/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/* \
  && groupadd --system mailer \
  && useradd --system --gid mailer --home-dir /app --shell /usr/sbin/nologin mailer

COPY --from=build --chown=mailer:mailer /workspace/target/app.jar /app/app.jar

USER mailer
EXPOSE 8080

# Flyway runs on first boot against the configured MariaDB.
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
