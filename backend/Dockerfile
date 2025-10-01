# Multi-stage Dockerfile

# Этап 1: Сборка приложения
FROM eclipse-temurin:22-jdk AS builder
WORKDIR /app

# Устанавливаем maven
RUN apt-get update && apt-get install -y maven

# Копируем pom и исходники
COPY pom.xml .
COPY src ./src

# Сборка с кешем для ~/.m2
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests

# Этап 2: Runtime-образ для продакшена
FROM eclipse-temurin:22-jre-alpine
WORKDIR /app

# Создаём пользователя для запуска приложения
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001 -G appgroup

# Создаем директории для SSL и загрузок
RUN mkdir -p /app/uploads /etc/ssl && \
    chown -R appuser:appgroup /app/uploads /etc/ssl

# Копируем собранный jar из builder stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# Переключаемся на пользователя appuser
USER appuser

# Порт приложения
EXPOSE 8300

# Переменные окружения для JVM
ENV JAVA_OPTS=""

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]