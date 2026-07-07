# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

# Dependencies zuerst cachen (nur neu laden wenn pom.xml sich ändert)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B --quiet || true

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -B -DskipTests --quiet

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /build/target/StoneUniverse-1.0.0.jar app.jar

# config.yml und hosts.yml werden als Volume gemountet – nicht im Image
VOLUME ["/app/config"]

ENTRYPOINT ["java", "-jar", "app.jar"]
