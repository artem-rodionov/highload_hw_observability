# HW1: Инструменты автоматизации в Grafana (Observability)

## Стек технологий
* **Микросервис (CRUD)**: Java 26, Spring Boot 4.1.1, Spring AOP, Micrometer, H2 (in-memory).
* **Генератор дашбордов**: Go, Grafana Foundation SDK.
* **Нагрузочное тестирование**: Grafana k6.
* **Инфраструктура**: Docker, Docker Compose.

## Как запустить

Для запуска всей системы вам понадобятся только установленные Docker и Docker Compose. 

Запуск docker compose:
```bash
docker compose up
```

По идее должно сразу работать, но у меня была проблемав том, что генерация дашбордов видимо слишком рано начинала работать, поэтому для воспроизведения рекомендую запустить контейнеры по очереди в следующем порядке:

crud -> victoriametrics -> grafana -> dashboard_generator -> k6

Первые три можно сразу запустить, чуть подождать и остальное.
