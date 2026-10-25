# java-list

java-list is an email newsletter and mailing-list application written in Java. It follows the same idea as [phpList](https://www.phplist.org/): keep subscriber lists, write campaigns, and send them by email.

## Status

Platform tracer is in place: Compose runs the Spring Boot app with MariaDB and Flyway. Feature work continues by epic.

## Goals

- Store subscriber lists and subscription preferences
- Compose and send email campaigns
- Support subscribe and unsubscribe
- Record basic delivery results for each campaign

## Getting started

Requires Docker and (for local builds) JDK 21+ and Maven 3.9+.

```bash
git clone https://github.com/rakhitha-karunarathne/java-list.git
cd java-list
docker compose up -d --build
curl http://127.0.0.1:8080/actuator/health
```

Stack pins: Java 21, Spring Boot 4.1.2, MariaDB 12.3.3 (see architecture spine). App code lives in `mailer/`.

Optional Compose profiles:

```bash
# phpMyAdmin on http://127.0.0.1:8081 only (never publish on a public edge)
docker compose --profile tools up -d

# GreenMail SMTP 3025 / IMAP 3143 for tests; point app with SPRING_PROFILES_ACTIVE=test
docker compose --profile test up -d
```

Default / production-shaped Compose omits GreenMail (app + MariaDB only; phpMyAdmin stays behind `tools`).
