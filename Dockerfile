# ------------------------------------------------------------------------------
# Stage 1: Build Image
# ------------------------------------------------------------------------------
FROM gradle:8.8-jdk21-alpine AS builder

WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts ./

# Download dependencies (cache layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build distribution jar
RUN gradle installDist --no-daemon

# ------------------------------------------------------------------------------
# Stage 2: Runtime Image (Minimal Lightweight JRE)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security best practice
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy installed binary from builder stage
COPY --from=builder /app/build/install/KopiKita-POS /app

# Volume for persistent SQLite database
VOLUME /app/data

# Run application
ENTRYPOINT ["/app/bin/KopiKita-POS"]
