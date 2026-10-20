FROM postgres:16-alpine

# El esquema y el seed se incluyen en la imagen para que PostgreSQL pueda
# leerlos al inicializar el volumen, sin depender de permisos del host.
COPY sql/db_MuniQuejas.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY sql/seed_usuarios.sql /docker-entrypoint-initdb.d/02-seed.sql
