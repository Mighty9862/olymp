# Multi-stage Dockerfile: сборка с Maven на eclipse-temurin:22-jdk и runtime на eclipse-temurin:22-jre-alpine

# Этап 1: Сборка приложения
FROM eclipse-temurin:22-jdk AS builder
WORKDIR /app

# Устанавливаем maven (образ на базе Debian)
RUN apt-get update && apt-get install -y maven

# Копируем pom и исходники
COPY pom.xml .
COPY src ./src

# Сборка с кешем для ~/.m2 (требует BuildKit: DOCKER_BUILDKIT=1)
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests

# Этап 2: Runtime-образ для продакшена
FROM eclipse-temurin:22-jre-alpine
WORKDIR /app

# Создаём небезопасного пользователя для запуска приложения (UID/GID 1001)
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001 -G appgroup

# Копируем собранный jar из builder stage (универсально: любой jar в target/)
COPY --from=builder /app/target/*.jar app.jar

# Устанавливаем владельца файла на созданного пользователя
RUN chown appuser:appgroup /app/app.jar

# Переключаемся на небезопасного пользователя
USER appuser

# Порт соответствует application.properties (server.port=8080)
EXPOSE 8080

# Позволяем передавать дополнительные опции JVM через переменную окружения
ENV JAVA_OPTS=""

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]