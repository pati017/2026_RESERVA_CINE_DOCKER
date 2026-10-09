-- show_at es la hora de pared de la sala (LocalDateTime en Java), no un instante.
-- Se pasa de TIMESTAMPTZ a TIMESTAMP conservando la hora "de reloj" definida en los datos de muestra
-- (20:00 sigue siendo 20:00, sin depender de la zona horaria de la JVM).
ALTER TABLE screenings
    ALTER COLUMN show_at TYPE TIMESTAMP USING (show_at AT TIME ZONE 'UTC');

-- Invariante también a nivel de base de datos: nunca más asientos libres que la capacidad.
ALTER TABLE screenings
    ADD CONSTRAINT chk_available_le_total CHECK (available_seats <= total_seats);
