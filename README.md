#  Система Управления Банковскими Картами
Система для управления банковскими картами с возможностью создания карт, переводов между счетами и администрирования пользователей.

📋 Функциональность
🔐 Аутентификация и авторизация
JWT токены для безопасной аутентификации

Ролевая модель: ADMIN и USER

Spring Security для защиты endpoints

💳 Возможности пользователя (USER)
Просмотр своих карт с пагинацией

Создание новых карт

Блокировка/активация своих карт

Переводы между своими картами

Просмотр баланса

⚙️ Возможности администратора (ADMIN)
Просмотр всех карт в системе

Создание карт для любого пользователя

Блокировка/активация любых карт

Управление пользователями

🛠 Технологии
Java 17+

Spring Boot 3.x

Spring Security + JWT

Spring Data JPA

PostgreSQL / MySQL

Liquibase для миграций

Docker & Docker Compose

Maven для сборки

JUnit 5 + Mockito для тестирования

🚀 Быстрый старт
Предварительные требования
Java 17 или выше

Maven 3.6+

PostgreSQL 15+ или Docker

1. Клонирование и настройка
bash
git clone <repository-url>
cd bank-card-system
2. Настройка базы данных
Вариант A: Локальная PostgreSQL
sql
CREATE DATABASE bankcard_db;
CREATE USER bankuser WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE bankcard_db TO bankuser;
Вариант B: Docker Compose (рекомендуется)
bash
docker-compose up -d
3. Конфигурация приложения
Создайте application.yml в src/main/resources/:

yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankcard_db
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: true
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml

app:
  jwt-secret: "mySuperSecretKeyForJWT123456789012345678901234567890"
  jwt-expiration-milliseconds: 86400000
  encryption-key: "1234567890123456"

logging:
  level:
    ru.effectivemobile: DEBUG
4. Сборка и запуск
bash
# Сборка проекта
mvn clean package

# Запуск приложения
mvn spring-boot:run

# Или запуск собранного JAR
java -jar target/bank-card-system-0.0.1-SNAPSHOT.jar
Приложение будет доступно по адресу: http://localhost:8080

📊 API Endpoints
🔐 Аутентификация
Регистрация пользователя
http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "user123",
  "password": "password123",
  "fullName": "John Connor",
  "email": "john.connor@example.com"
}
Вход в систему
http
POST /api/auth/signin
Content-Type: application/json

{
  "username": "user123",
  "password": "password123"
}
Ответ:

json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
💳 Управление картами (USER)
Создание карты
http
POST /api/cards
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "cardHolderName": "JOHN CONNOR",
  "expiryDate": "2027-12"
}
Получение списка своих карт
http
GET /api/cards/my?page=0&size=10&sort=id
Authorization: Bearer <jwt-token>
Блокировка карты
http
PUT /api/cards/{cardId}/block
Authorization: Bearer <jwt-token>
Активация карты
http
PUT /api/cards/{cardId}/activate
Authorization: Bearer <jwt-token>
💸 Переводы между картами
Перевод между своими картами
http
POST /api/transfers
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "fromCardId": 1,
  "toCardId": 2,
  "amount": 100.50,
  "description": "Перевод на основную карту"
}
⚙️ Административные функции (ADMIN)
Получение всех карт
http
GET /api/admin/cards?page=0&size=20
Authorization: Bearer <jwt-token>
Создание карты для пользователя
http
POST /api/admin/cards
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "cardHolderName": "JANE SMITH",
  "expiryDate": "2028-06",
  "userId": 3
}
🐳 Docker развертывание
Запуск с Docker Compose

# Запуск всей системы (приложение + БД)
docker-compose up -d

# Просмотр логов
docker-compose logs -f app

# Остановка
docker-compose down
Dockerfile
dockerfile
FROM openjdk:21-jdk-slim
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
docker-compose.yml
yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: bankcard_db
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: 1234
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/bankcard_db
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: 1234
    depends_on:
      - postgres

volumes:
  postgres_data:
🧪 Тестирование
Запуск тестов

# Все тесты
mvn test

# Только unit тесты
mvn test -Dtest="*Test"

# Только интеграционные тесты
mvn test -Dtest="*IntegrationTest"

# С генерацией отчета покрытия
mvn jacoco:report
Структура тестов
Unit тесты: *Test.java

Интеграционные тесты: *IntegrationTest.java

Mock тесты: Используют Mockito для изоляции компонентов

🔒 Безопасность
Особенности безопасности
🔐 JWT токены с expiration

🎭 Ролевой доступ к endpoints

🔒 Шифрование номеров карт (AES)

🎭 Маскирование данных при отображении

💾 Подготовленные SQL запросы для предотвращения инъекций

🔑 BCrypt для хеширования паролей

Настройка безопасности
yaml
app:
  jwt-secret: "your-256-bit-secret"  # Минимум 32 символа
  jwt-expiration-milliseconds: 86400000  # 24 часа
  encryption-key: "16-char-enc-key"  # Ровно 16 символов для AES
📊 Миграции базы данных
Liquibase миграции
Миграции находятся в src/main/resources/db/changelog/

Создание новой миграции

# Создать файл миграции
# Миграции автоматически применяются при запуске приложения
Ручной запуск миграций

mvn liquibase:update
🐛 Отладка и логирование
Уровни логирования
yaml
logging:
  level:
    ru.effectivemobile: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
Просмотр логов
bash
# В development
tail -f logs/application.log

# В Docker
docker-compose logs -f app
📈 Мониторинг
Health check
http
GET /actuator/health
Информация о приложении
http
GET /actuator/info
🔄 CI/CD
GitHub Actions пример
yaml
name: Build and Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests
        run: mvn test
🤝 Вклад в проект
Установка для разработки
Форкните репозиторий

Создайте feature ветку: git checkout -b feature/amazing-feature

Сделайте коммит: git commit -m 'Add amazing feature'

Запушьте ветку: git push origin feature/amazing-feature

Создайте Pull Request

Code style

# Проверка стиля кода
mvn checkstyle:check

# Форматирование кода
mvn spotless:apply
📞 Поддержка
Полезные команды

# Пересоздать базу данных
docker-compose down -v && docker-compose up -d

# Просмотр логов приложения
docker-compose logs -f app

# Запуск с профилем development
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Генерация документации API
mvn springdoc:generate
Частые проблемы
Ошибка подключения к БД: Проверьте настройки в application.yml

JWT ошибки: Убедитесь, что jwt-secret достаточно длинный

Миграции не применяются: Проверьте путь к changelog файлам

