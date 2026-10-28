FROM postgres:16-alpine

# Los scripts de /docker-entrypoint-initdb.d solo se ejecutan cuando el
# volumen de datos está vacío. Para reinicializar: docker compose down -v.
COPY sql/db_MuniQuejas.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY sql/seed_usuarios.sql /docker-entrypoint-initdb.d/02-seed.sql
