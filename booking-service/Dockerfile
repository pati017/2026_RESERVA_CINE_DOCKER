# ==========================================
# Etapa 1: Compilación con Java 25
# ==========================================
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# 1. Copiar pom.xml para cachear dependencias
COPY pom.xml .

# 2. Descargar dependencias
RUN mvn dependency:go-offline -B

# 3. Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Imagen Final (Runtime Java 25)
# ==========================================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Crear usuario sin privilegios por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el jar compilado
COPY --from=build /app/target/*.jar app.jar

EXPOSE 51200

# JAVA_OPTS se puede sobreescribir desde docker-compose (memoria, flags, etc.)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
