# KillrVideo v2 - Java Spring Boot Backend

[![Build KillrVideo Java backend](https://github.com/KillrVideo/kv-be-java-springboot3-dataapi-collections/actions/workflows/build.yaml/badge.svg)](https://github.com/KillrVideo/kv-be-java-springboot3-dataapi-collections/actions/workflows/build.yaml)

Date: June 2025

A reference backend for the KillrVideo sample application rebuilt for 2025 using **Java**, **Spring Boot** and **DataStax Astra DB**.

---

## Overview
This repo demonstrates modern API best-practices with:

* Spring Boot for typed request/response models
* Role-based JWT auth
* Restful Cassandra (Astra DB) Data API client via `astra-db-java`
* Micro-service friendly layout – or run everything as a monolith

---

## Prerequisites
1. **Java 21+** (use jenv or sdkman)
2. **Maven** for dependency management:
```xml
<dependency>
    <groupId>com.datastax.astra</groupId>
    <artifactId>astra-db-java</artifactId>
    <version>2.0.0</version>
</dependency>
```
3. A **DataStax Astra DB** serverless database – grab a free account.
4. Docker or Podman (optional) if you want to run the individual services in containers.

## Setup & Configuration
```bash
# clone
git clone git@github.com:KillrVideo/killrvideo-java-2025.git
cd killrvideo-java-2025

# build and install deps
mvn clean package
```

Database collections:
1. Create a new keyspace named `killrvideo_dataapi`.
2. Create the following non-vector-enabled collections:
 - comments
 - users
 - ratings
3. Create the following vector-enabled collection:
 - videos (with a 384-dimensional vector)

Environment variables (via `export`):

| Variable | Description |
|----------|-------------|
| `ASTRA_DB_API_ENDPOINT` | REST endpoint for your Astra DB instance |
| `ASTRA_DB_APPLICATION_TOKEN` | Token created in Astra UI |
| `ASTRA_DB_NAMESPACE` | `killrvideo_dataapi` |

Edit `application.yml`:
 - Generate and change the `killrvideo.jwt.secret` key (or use the default).
 - Generate your own keystore for your self-signed TLS certificate.

---

## Running the Application
```bash
java -jar target/killrvideo-java-service-1.0.0-SNAPSHOT.jar --logging.level.com.killrvideo=DEBUG --logging.level.org.springframework.security=DEBUG
```

---

## Building & Running with Docker or Podman
```bash
docker build -t killrvideo-2025-java .
docker run -p 8443:8443 -e ASTRA_DB_NAMESPACE=killrvideo_dataapi -e ASTRA_DB_APPLICATION_TOKEN=your_token -e ASTRA_DB_API_ENDPOINT=your_endpoint killrvideo-2025-java
```

Or

```bash
podman build -t killrvideo-2025-java .
podman run -p 8443:8443 -e ASTRA_DB_NAMESPACE=killrvideo_dataapi -e ASTRA_DB_APPLICATION_TOKEN=your_token -e ASTRA_DB_API_ENDPOINT=your_endpoint killrvideo-2025-java
```
