# 🏫 Школьная платформа для олимпиад

![Java](https://img.shields.io/badge/Java-22-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)

Веб-приложение для управления школьными олимпиадами с системой регистрации участников, администрирования и автоматизированной выгрузкой данных.

## 🚀 Возможности

### 👥 Для пользователей
- **Регистрация** с заполнением персональных данных
- **Выбор олимпиад** для участия
- **Просмотр профиля** с дешифрованными данными
- **Просмотр новостей** и карусели изображений

### ⚡ Для администраторов
- **Управление пользователями** и ролями
- **Создание и удаление олимпиад**
- **Управление новостями** с указанием дат
- **Загрузка изображений** для карусели
- **Экспорт данных** в Excel-формат
- **Автоматическое резервное копирование**

### 🛡️ Безопасность
- **JWT аутентификация**
- **Шифрование персональных данных** (AES-256)
- **Ролевая модель доступа** (ADMIN/USER)
- **CORS настройки** для фронтенда

## 📦 Технологический стек

- **Backend**: Java 22, Spring Boot 3, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 16
- **Authentication**: JWT (HS512)
- **Documentation**: OpenAPI 3 (Swagger)
- **Containerization**: Docker, Docker Compose
- **File Storage**: Локальная файловая система
- **Monitoring**: Spring Boot Actuator

## 🏗️ Архитектура

```
olymp_schools/
├── 📁 src/main/java/org/example/
│   ├── config/              # Конфигурации (Security, CORS, OpenAPI)
│   ├── controller/          # REST API контроллеры
│   ├── dto/                 # Data Transfer Objects
│   ├── entity/              # JPA сущности
│   ├── enums/               # Перечисления (роли, гендер)
│   ├── exception/           # Кастомные исключения
│   ├── repository/          # Spring Data репозитории
│   ├── security/            # JWT аутентификация
│   ├── service/             # Бизнес-логика
│   └── util/                # Утилиты (JWT, шифрование)
├── 📁 backups/              # Автоматические бэкапы БД
├── 📁 uploads/              # Загруженные изображения
├── 🐳 docker-compose.yml    # Оркестрация контейнеров
├── 📦 Dockerfile           # Сборка приложения
├── 🔧 backup-manager.sh    # Управление бэкапами
├── 🚀 deploy.sh            # Скрипт развертывания
├── 📊 log-config.sh        # Мониторинг логов
└── 📄 README.md            # Документация
```

## ⚡ Быстрый старт

### Предварительные требования

- **Docker** 20.10+
- **Docker Compose** 2.0+
- **4 ГБ+ оперативной памяти**

### Запуск проекта

1. **Клонируйте репозиторий** (или скопируйте файлы проекта)

2. **Запустите автоматическое развертывание**:
```bash
chmod +x deploy.sh backup-manager.sh log-config.sh
./deploy.sh
```

3. **Приложение доступно по адресам**:
    - 🌐 **Главное приложение**: http://localhost:8300
    - 📚 **Swagger документация**: http://localhost:8300/swagger-ui.html
    - 🗄️ **База данных**: localhost:5433

### Остановка проекта
```bash
docker compose down
```

## 🔐 Администрирование

### Создание первого администратора

1. **Зарегистрируйтесь** через `/api/auth/register`
2. **Первый пользователь автоматически получает роль ADMIN**

### Ручное назначение администратора
```bash
POST /api/admin/assign/{email}
```

### Экспорт данных пользователей
- **Эндпоинт**: `GET /api/admin/export-users`
- **Формат**: Excel с российскими стандартами
- **Пароли**: Защищены (отображаются как `*`)

## 💾 Резервное копирование

### Автоматические бэкапы
- ⏰ **Расписание**: Ежедневно в 2:00
- 💾 **Хранение**: 7 дней
- 📁 **Директория**: `./backups/`

### Ручное управление бэкапами

```bash
# Создать бэкап
./backup-manager.sh create

# Восстановить из бэкапа
./backup-manager.sh restore backup-2024-01-15-10-30-00.dump

# Список бэкапов
./backup-manager.sh list

# Очистка старых бэкапов
./backup-manager.sh clean
```

### Восстановление из бэкапа
1. Остановите приложение: `docker-compose stop app`
2. Восстановите БД: `./backup-manager.sh restore <filename>`
3. Запустите приложение: `docker-compose start app`

## 📊 Мониторинг и логи

### Health Checks
- **Приложение**: `GET /actuator/health`
- **База данных**: Автоматические проверки каждые 30 секунд

### Просмотр логов
```bash
# Логи в реальном времени
./log-config.sh live

# Логи за последний час
./log-config.sh recent

# Поиск ошибок
./log-config.sh errors

# Статистика ресурсов
./log-config.sh stats
```

### Полезные команды Docker
```bash
# Статус контейнеров
docker-compose ps

# Логи конкретного сервиса
docker-compose logs app
docker-compose logs db

# Мониторинг ресурсов
docker stats

# Использование диска
docker system df
```

## 🛠️ API Endpoints

### 🔓 Публичные эндпоинты
- `POST /api/auth/register` - Регистрация
- `POST /api/auth/login` - Вход
- `GET /api/olympiads` - Список олимпиад
- `GET /api/news` - Новости
- `GET /api/carousel/images` - Изображения карусели

### 🔐 Защищенные эндпоинты
- `GET/PUT /api/profile` - Профиль пользователя
- `POST/DELETE /api/profile/olympiads` - Управление олимпиадами

### ⚡ Административные эндпоинты
- `POST/DELETE /api/admin/news` - Управление новостями
- `POST/DELETE /api/admin/olympiads` - Управление олимпиадами
- `POST/DELETE /api/admin/carousel` - Управление изображениями
- `GET /api/admin/export-users` - Экспорт пользователей

## 🔧 Настройка окружения

### Переменные окружения
```properties
# База данных
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/newsdb
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin

# Безопасность
JWT_SECRET=YourJWTSecretKey
AES_SECRET=YourAES256SecretKey

# Файлы
UPLOAD_DIR=/app/uploads
```

### Кастомизация портов
Измените в `docker-compose.yml`:
```yaml
ports:
  - "8080:8300"  # external:internal
```

## 🚨 Устранение неисправностей

### Проблема: Контейнеры не запускаются
```bash
# Проверка логов
docker-compose logs

# Пересборка образов
docker-compose build --no-cache

# Запуск с принудительным пересозданием
docker-compose up -d --force-recreate
```

### Проблема: Недостаточно памяти
```bash
# Очистка Docker
docker system prune -a

# Увеличение лимитов памяти Docker
```

### Проблема: База данных не подключена
```bash
# Проверка состояния БД
docker-compose logs db

# Ручной запуск БД
docker-compose up -d db

# Ожидание готовности БД
docker-compose exec db pg_isready -U admin
```

## 📈 Производительность

### Рекомендуемые настройки
- **RAM**: 4 ГБ минимум, 8 ГБ рекомендуется
- **CPU**: 2+ ядра
- **Диск**: 20 ГБ свободного места

### Оптимизация
- Автоматическое масштабирование JVM памяти
- Кэширование запросов к базе данных
- Оптимизированные Docker образы

## 🔄 Обновление

### Обновление до новой версии
```bash
# Остановка текущей версии
docker-compose down

# Обновление кода (если используется git)
git pull origin main

# Пересборка и запуск
./deploy.sh
```


