COMPOSE = docker compose -f docker/docker-compose.yml

.PHONY: help infra-up infra-down infra-restart infra-clean deps-up ps logs docker-rebuild kafka-consume build

help:
	@echo ""
	@echo "  Sistema de Reserva de Cine - Comandos disponibles"
	@echo "  --------------------------------------------------"
	@echo "  make infra-up                    Compila las imagenes y levanta TODO (BD, Kafka y los 3 microservicios)"
	@echo "  make infra-down                  Detiene y elimina los contenedores"
	@echo "  make infra-restart               Reinicia todo (sin recompilar)"
	@echo "  make infra-clean                 Elimina contenedores y volumenes (BD y Kafka desde cero)"
	@echo "  make deps-up                     Solo PostgreSQL + Kafka (para correr los servicios desde el IDE)"
	@echo "  make docker-rebuild service=xxx  Recompila UN servicio (auth-service | movie-service | booking-service)"
	@echo "  make kafka-consume               Lee los eventos del topic cinema.bookings"
	@echo "  make ps                          Estado de los contenedores"
	@echo "  make logs service=xxx            Logs de un contenedor"
	@echo ""

# Construye las imagenes (Maven corre DENTRO de Docker) y espera a que todo este "healthy".
infra-up:
	$(COMPOSE) up -d --build --wait --wait-timeout 600
	@echo ""
	@echo "Todo levantado."
	@echo "  auth-service     http://localhost:50003/actuator/health"
	@echo "  movie-service    http://localhost:51100/api/v1/swagger-ui.html"
	@echo "  booking-service  http://localhost:51200/api/v1/swagger-ui.html"
	@echo "  Kafka UI         http://localhost:9000"
	@echo "  PostgreSQL       localhost:5432 (moviedb, bookingdb)"

infra-down:
	$(COMPOSE) down

infra-restart:
	$(COMPOSE) down
	$(COMPOSE) up -d --wait --wait-timeout 600

infra-clean:
	$(COMPOSE) down -v
	@echo "Contenedores y volumenes eliminados."
	@echo "Las bases de datos seran creadas nuevamente con infra-up."

# Modo desarrollo: infraestructura en Docker y los microservicios desde el IDE (puertos en localhost).
deps-up:
	$(COMPOSE) up -d --wait postgres-ms kafka kafka-ui
	@echo "PostgreSQL: localhost:5432 | Kafka: localhost:29092 | Kafka UI: http://localhost:9000"

ps:
	$(COMPOSE) ps

logs:
	docker logs -f $(service)

docker-rebuild:
	$(COMPOSE) build $(service)
	$(COMPOSE) up -d --no-deps $(service)
	@echo "$(service) reconstruido y reiniciado"

kafka-consume:
	docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic cinema.bookings --from-beginning
