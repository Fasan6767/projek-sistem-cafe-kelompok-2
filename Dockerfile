# ==========================================
# Stage 1: Build Application with Gradle
# ==========================================
FROM gradle:8.10-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle installDist --no-daemon

# ==========================================
# Stage 2: Minimal Lightweight Runtime Image
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/install/kopikita-pos /app

ENTRYPOINT ["/app/bin/kopikita-pos"]
