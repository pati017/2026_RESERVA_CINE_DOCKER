# Sistema de Reserva de Cine — Spring Boot 4 / Java 25
# Patty Castro Carlo

Dos microservicios (`movie-service` y `booking-service`) que se comunican con un **SAGA de dos pasos** (reserva y
cancelación), protegidos con **JWT RS256** emitido por `auth-service`. 

| Servicio | Puerto | Base de datos | Swagger UI |
|---|---|---|---|
| auth-service (emisor de JWT, reutilizado) | 50003 | — | — |
| movie-service (catálogo + funciones) | 51100 | `moviedb` | http://localhost:51100/api/v1/swagger-ui.html |
| booking-service (reservas + SAGA) | 51200 | `bookingdb` | http://localhost:51200/api/v1/swagger-ui.html |
| PostgreSQL (Docker) | 5432 | `moviedb`, `bookingdb` | — |
| Kafka (Docker, extra opcional; igual al del curso) | 29092 | topic `cinema.bookings` | — |
| Kafka UI (Docker, ver eventos en el navegador) | 9000 | — | http://localhost:9000 |

```
Cliente / Postman / Swagger ──JWT──► movie-service   (51100) ──► moviedb
                │                        ▲
                │                        │ RestClient @LoadBalanced + @Retry + Circuit Breaker (reenvía el mismo JWT)
                └──JWT──► booking-service (51200) ──► bookingdb      └─► Kafka (cinema.bookings)
auth-service (50003): POST /api/v1/auth/login → JWT   |   GET /oauth2/jwks → llaves públicas
```

## URLs rápidas

| Qué | URL |
|---|---|
| **Kafka UI (ver los eventos de las reservas)** | **http://localhost:9000** → *Topics* → `cinema.bookings` → *Messages* |
| Swagger movie-service | http://localhost:51100/api/v1/swagger-ui.html |
| Swagger booking-service | http://localhost:51200/api/v1/swagger-ui.html |
| JWKS de auth-service | http://localhost:50003/oauth2/jwks |

---
## 1. Requisitos Previos

- **Docker Desktop** + Docker Compose (`docker -v`) y `make` → es lo único necesario para levantar todo.
- Java 25 y Maven 3.9+ → solo si quieres correr los servicios desde el IDE o con `mvn` (sección 3). Con Docker **no** hacen falta:
  la compilación ocurre dentro de la imagen.

---

## 2. Cómo Levantar

### Todo con un solo comando (Docker)

```bash
make infra-up
```

Construye las imágenes de `auth-service`, `movie-service` y `booking-service` (Maven corre **dentro** de Docker, multi-etapa con
Java 25) y levanta PostgreSQL, Kafka, Kafka UI y los tres microservicios en orden, esperando a que cada uno esté *healthy*.
La primera vez tarda varios minutos (descarga de dependencias); las siguientes usan la caché de Docker.

| Contenedor | Puerto | Depende de |
|---|---|---|
| postgres-ms | 5432 | — |
| kafka / kafka-ui | 29092 / 9000 | — |
| auth-service | 50003 | — |
| movie-service | 51100 | postgres-ms, auth-service |
| booking-service | 51200 | postgres-ms, kafka, auth-service, movie-service |

Dentro de Docker los servicios se hablan por **nombre** (`auth-service`, `kafka`, `movie-service`, `postgres-ms`); esas direcciones
se inyectan desde `docker/docker-compose.yml` mediante variables de entorno (`AUTH_JWKS_URI`, `KAFKA_BOOTSTRAP_SERVERS`,
`MOVIE_SERVICE_URI`, `DATASOURCE_URL_*`). Sin variables, los `application.yml` usan `localhost` (modo IDE).

| Comando | Qué hace |
|---|---|
| `make ps` | Estado y salud de los contenedores |
| `make logs service=booking-service` | Logs de un servicio |
| `make docker-rebuild service=movie-service` | Recompila y reinicia **solo** ese servicio tras cambiar código |
| `make infra-down` | Detiene todo (conserva los datos) |
| `make infra-clean` | Borra contenedores **y datos** (BD y Kafka desde cero) |

> Si es la primera vez o se agregaron nuevas bases de datos: `make infra-clean && make infra-up`

---
## 3. Alternativa: ejecutar cada proyecto desde el IDE / Maven (modo desarrollo)

Útil para depurar. Levanta solo la infraestructura con `make deps-up` (PostgreSQL + Kafka). Si ya hiciste `make infra-up`,
detén primero el contenedor del servicio que vas a correr desde el IDE para liberar su puerto, por ejemplo
`docker compose -f docker/docker-compose.yml stop movie-service`.

Cada servicio es un proyecto Maven separado (su propio `pom.xml`). **Orden recomendado:**
`auth-service` → `movie-service` → `booking-service`. Elige la opción que te funcione; todas hacen lo mismo.

| Servicio | Carpeta | Clase principal |
|---|---|---|
| auth-service | `auth-service/` | `com.mitocode.ecommerce.auth.AuthServiceApplication` |
| movie-service | `movie-service/` | `com.cinema.movie_service.MovieServiceApplication` |
| booking-service | `booking-service/` | `com.cinema.booking_service.BookingServiceApplication` |

### Opción A — IntelliJ IDEA / VS Code (la más simple, sin usar Maven por consola)

1. `File → Open` y elige la carpeta del servicio (por ejemplo `movie-service`, **no** la raíz). IntelliJ detecta el `pom.xml`
   y descarga las dependencias solo (Maven viene incluido en el IDE).
2. Verifica que el SDK del proyecto sea **JDK 25** (`File → Project Structure → Project`).
3. Abre la clase principal de la tabla y pulsa el botón ▶ **Run**.
4. Repite en otra ventana (o con `File → Open → Add to current window`) para los otros dos servicios.

En VS Code: extensión *Extension Pack for Java* + *Spring Boot Extension Pack*, abre la carpeta del servicio y usa *Run* sobre la clase principal.

### Opción B — Maven instalado (`mvn`)

```bash
mvn -f auth-service/pom.xml    spring-boot:run     # 1º 
mvn -f movie-service/pom.xml   spring-boot:run     # 2º
mvn -f booking-service/pom.xml spring-boot:run     # 3º
```

Una terminal por servicio. Si `mvn` no se reconoce, instala Maven 3.9+ y agrégalo al `PATH`
(https://maven.apache.org/download.cgi), o usa la opción A o C.

### Opción C — Generar el JAR y ejecutarlo con `java -jar`

Sirve cuando no quieres usar `spring-boot:run`. Primero construye el JAR (desde el IDE: panel *Maven → Lifecycle → package*,
o con `mvn`):

```bash
mvn -f movie-service/pom.xml package -DskipTests
java -jar movie-service/target/movie-service-0.0.1-SNAPSHOT.jar
```

Mismo patrón para los otros (`auth-service/target/*.jar`, `booking-service/target/*.jar`; el nombre exacto del JAR
está en la carpeta `target/`). Con `make`: `make build service=movie-service`.

### Opción D — Maven dentro de Docker (sin instalar Maven ni configurar el IDE)

Solo requiere Docker y funciona igual en cualquier sistema si los servicios usan la red del host. En **Linux**:

```bash
docker run --rm -it --network host -v "$PWD/movie-service":/app -w /app maven:3.9-eclipse-temurin-25 mvn spring-boot:run
```

En **Windows/macOS** `--network host` no es equivalente; ahí usa las opciones A, B o C.

### Qué necesita cada servicio para funcionar solo

| Servicio | Necesita | Qué puedes probar sin los demás |
|---|---|---|
| auth-service | nada | Login y `GET /oauth2/jwks` |
| movie-service | PostgreSQL (`moviedb`) | `GET /movies`, `GET /screenings` y Swagger (son públicos). Para las escrituras necesita además auth-service (valida el JWT) |
| booking-service | PostgreSQL (`bookingdb`), auth-service y movie-service | El SAGA completo. Kafka es opcional: sin Kafka la reserva funciona igual y el evento se omite |

### Comprobar que todo está arriba

```bash
curl -s -X POST http://localhost:50003/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"user","password":"user123"}'
curl -s http://localhost:50003/oauth2/jwks
curl -s http://localhost:51100/api/v1/movies          # Toy Story y Coco
curl -s http://localhost:51100/api/v1/actuator/health
curl -s http://localhost:51200/api/v1/actuator/health
```

Kafka (gráfico): abre **http://localhost:9000** en el navegador (Kafka UI).

### Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `release version 25 not supported` / `invalid target release` | El JDK activo no es el 25. Cambia `JAVA_HOME` o el SDK del IDE. |
| `Connection refused ... :5432` / `database "moviedb" does not exist` | PostgreSQL no está arriba o el volumen es antiguo: haz el reinicio desde cero de la sección 2. |
| `401` en todas las escrituras | Token vencido (dura 5 min) o auth-service apagado. Repite el login. |
| `Port 51100 was already in use` | Otro proceso usa el puerto; ciérralo o cambia `server.port`. |
| Warnings de Kafka (`Connection to node -1`) | Kafka no está levantado o el puerto no es `localhost:29092`. No afecta al SAGA; levanta Kafka o ignóralos. |
| Un contenedor queda `unhealthy` / `make infra-up` falla por timeout | Mira sus logs: `make logs service=movie-service`. Causas típicas: PostgreSQL con volumen viejo (`make infra-clean`) o falta de memoria en Docker Desktop (asígnale ≥ 4 GB). |
| `401` solo cuando los servicios corren en Docker | Pide el token a `http://localhost:50003/...` (el `iss` del token es `localhost:50003`); no uses `auth-service:50003` desde Postman. |
| `Port ... is already allocated` al hacer `make infra-up` | Un servicio está corriendo desde el IDE en el mismo puerto: ciérralo. |
| El IDE no ve las dependencias | `Maven → Reload All Maven Projects` y `clean`. |

## 4. Autenticación entre servicios

1. El cliente hace `POST http://localhost:50003/api/v1/auth/login` (`user/user123` o `admin/admin123`) y recibe `access_token`.
2. Cada servicio es *Resource Server*: valida la firma con las llaves públicas de `http://localhost:50003/oauth2/jwks`.
3. `booking-service` **reenvía el mismo JWT** a `movie-service` durante el SAGA (`BearerTokenRelayInterceptor`), así que
   movie-service autentica al usuario real. Sin token → `401` (ProblemDetail).
4. El `customerId` no viene en el body: se deriva del `sub` del JWT. Solo el dueño (o `ROLE_ADMIN`) lee o cancela su reserva (`403` si no).
5. El token dura **5 minutos** (`auth.jwt.access-token-ttl: 300`); si Swagger devuelve 401, vuelve a hacer login.

Público: Swagger UI, `/actuator/**`, `GET /movies/**`, `GET /screenings/**`. Todo lo demás exige JWT.

## 5. SAGA

`POST /bookings`: verificar función → crear PENDING (con snapshot de `movieTitle`) → `PATCH /screenings/{id}/reserve`
(`@Retry`: 3 intentos, 500 ms) → CONFIRMED. Si falla, `seatReserved`/`bookingId` deciden qué compensar y la reserva
nunca queda PENDING. `PATCH /bookings/{id}/cancel`: CANCELLED + liberar asiento *best-effort*.

| Caso | Resultado |
|---|---|
| Reserva exitosa | 201 + `Location`, status `CONFIRMED` |
| Sin asientos / función inexistente o inactiva | 422, no se crea booking |
| movie-service caído en el paso 3 (tras 3 intentos) | 503, booking `CANCELLED` |
| Circuit Breaker abierto | 503 inmediato, booking `CANCELLED` |
| Cancelar dos veces | 422 |
| Reserva de otro usuario | 403 |

## 6. Cómo probar (Postman)

1. Importa `postman/Cinema_System.postman_collection.json` (trae sus variables; **no** necesita Environment).
2. Carpeta **Auth** → *Login (user)*: guarda `{{TOKEN}}` automáticamente (válido 5 min).
3. (Opcional) El CRUD de películas usa `{{NEW_MOVIE_ID}}` (la que crea *Create Movie*); *Create Screening* usa `{{MOVIE_ID}}` (Toy Story) y por eso *Delete Movie* no lo afecta.
3. **Create Booking (SAGA)** con `SCREENING_ID` de Toy Story → 201 `CONFIRMED` (guarda `{{BOOKING_ID}}`).
4. **Cancel Booking** → 200 `CANCELLED`; repetirlo → 422.
5. Para el 422 de "sin asientos": *Create Screening* con `totalSeats: 1` y reserva dos veces.

## 7. Extras opcionales implementados (2)

**1. Circuit Breaker** (`resilience4j.circuitbreaker.instances.movieService`) sobre las tres llamadas a movie-service.
Se abre cuando falla ≥ 50 % de las últimas 10 llamadas (mínimo 5); queda abierto 10 s y luego prueba 3 llamadas (HALF_OPEN).
El **fallback** responde de inmediato `503` (`MovieServiceCircuitOpenException`) sin esperar timeouts y el SAGA compensa
igual (la reserva queda CANCELLED). Un 422 (sin asientos) no cuenta como fallo. `@Retry` envuelve al circuito y no reintenta
con el circuito abierto.
*Demo:* detén movie-service → haz 5–6 `POST /bookings` → las primeras dan 503 tras 3 reintentos y, con el circuito abierto,
responden al instante con `"title": "Circuit Breaker Open"`. Reinicia movie-service y a los 10 s se recupera solo.

**2. Kafka:** al confirmarse una reserva se publica `BookingConfirmedEvent` (JSON, clave = bookingId) en el topic
`cinema.bookings`. Es best-effort: se publica fuera del SAGA, así que si Kafka está caído la reserva sigue CONFIRMED.
*Demo gráfica:* abre **http://localhost:9000** (Kafka UI) → *Topics* → `cinema.bookings` → pestaña *Messages*
(clave = bookingId, valor = JSON del evento). El topic aparece después de la primera reserva confirmada.

*Demo por consola:* en otra terminal:

```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic cinema.bookings --from-beginning
# (con make:  make kafka-consume)
```

