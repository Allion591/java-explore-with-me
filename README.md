# java-explore-with-me

Многомодульное приложение для публикации событий.

## Технологии
- Java
- Maven
- Spring Boot
- SQL (PostgreSQL / H2)
- Docker

## Структура проекта
- `pom.xml` — конфигурация Maven
- `src/main/java` — исходный код приложения (контроллеры, сервисы, репозитории)
- `src/main/resources` — конфигурация (`application.yml` / `application.properties`)
- `src/test` — модульные тесты

## Требования
- JDK 17+ 
- Maven 3.6+ 
- Запущенная СУБД (Postgres) с созданной БД
- IntelliJ IDEA (рекомендуется для разработки на Windows)

## Быстрый запуск (Windows)
1. Установите переменные окружения / конфигурацию БД в `src/main/resources/application.yml` или в системных переменных:
    - `spring.datasource.url`
    - `spring.datasource.username`
    - `spring.datasource.password`
2. Собрать и запустить (Maven Wrapper):
    - Сборка: `mvnw.cmd clean package`
    - Запустить из IDE: запустить класс с `@SpringBootApplication`
    - Запуск jar: `java -jar target\[artifactId]-[version].jar`
    - Альтернативно: `mvnw.cmd spring-boot:run`

## Конфигурация
Пример (в `application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/[db_name]
    username: [db_user]
    password: [db_pass]
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
server:
  port: 8080
