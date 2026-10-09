-- PostgreSQL ejecuta este script la PRIMERA VEZ que arranca el contenedor (volumen vacío).
-- Para reaplicarlo: make infra-clean && make infra-up
CREATE DATABASE moviedb;
CREATE DATABASE bookingdb;

GRANT ALL PRIVILEGES ON DATABASE moviedb TO admin;
GRANT ALL PRIVILEGES ON DATABASE bookingdb TO admin;
