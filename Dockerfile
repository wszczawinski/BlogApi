# Build stage
FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Install nginx and supervisor
RUN apt-get update && apt-get install -y nginx supervisor && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Create directories with proper permissions
RUN mkdir -p /app/logs /app/media && \
    chown -R www-data:www-data /app/media && \
    chmod -R 755 /app/media

# These volumes will persist across container recreations
VOLUME /app/logs
VOLUME /app/media

ENV SERVER_PORT=${SERVER_PORT} \
    SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL} \
    SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME} \
    SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD} \
    SPRING_JPA_SHOW_SQL=${SPRING_JPA_SHOW_SQL} \
    SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=${SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL} \
    JWT_SECRET=${JWT_SECRET} \
    CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS} \
    APP_MEDIA_MAX_FILE_SIZE=${APP_MEDIA_MAX_FILE_SIZE} \
    APP_MEDIA_UPLOAD_DIR=${APP_MEDIA_UPLOAD_DIR} \
    APP_MEDIA_MAX_FILES=${APP_MEDIA_MAX_FILES} \
    APP_PAGINATION_POSTS_PER_PAGE=${APP_PAGINATION_POSTS_PER_PAGE}

EXPOSE 80 8080
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]