# Despliegue en una VM de Azure con Docker Compose

Esta es la guía vigente para la demo de 3 días. Reemplaza al despliegue
serverless (Static Web Apps + Container Apps + PostgreSQL Flexible Server).

> Si ya creaste recursos del plan anterior, elimínalos cuando confirmes que no
> los necesitas para no seguir pagando. La VM crea una base de datos nueva a
> partir de `sql/db_MuniQuejas.sql` y `sql/seed_usuarios.sql`; si necesitas
> datos de una base anterior, expórtalos antes de borrarla.

## Arquitectura

Todo corre en una sola VM de Azure:

```text
Internet
   |
   | puerto 80
   v
Nginx (contenedor frontend)
   |-- sirve Angular (archivos estáticos)
   |-- /auth, /quejas, /usuarios, /api, /actuator -> backend:8080
   |-- /evidencias -> volumen compartido de imágenes
   |
   +--> backend (Spring Boot, red interna)
           |
           +--> db (PostgreSQL, red interna)
```

- Solo se publica el puerto 80 del contenedor `frontend`.
- `backend` y `db` **no** se publican al exterior.
- PostgreSQL y las evidencias usan volúmenes Docker.
- El frontend se compila con `BACKEND_URL=same-origin`: las llamadas van al
  mismo host y Nginx las redirige al backend. Así no hay CORS entre frontend y
  backend.
- No hace falta compilar Angular ni construir imágenes Docker en el equipo
  local: la VM lo hace con `docker compose up -d --build`.

## 1. Crear la VM

Puedes ejecutar estos comandos en Azure Cloud Shell o en un equipo con Azure
CLI. Ajusta los nombres si ya están ocupados.

```bash
export RG="rg-muniques-demo"
export LOCATION="eastus2"
export VM="vm-muniques-demo"
export ADMIN_USER="azureuser"

az login
az group create --name "$RG" --location "$LOCATION"

az vm create \
  --resource-group "$RG" \
  --name "$VM" \
  --image Ubuntu2404 \
  --size Standard_B2ms \
  --admin-username "$ADMIN_USER" \
  --generate-ssh-keys \
  --public-ip-sku Standard \
  --os-disk-size-gb 64 \
  --storage-sku StandardSSD_LRS

# Abrir solo HTTP. El puerto 22 ya queda abierto para SSH.
az vm open-port \
  --resource-group "$RG" \
  --name "$VM" \
  --port 80 \
  --priority 1001

# Obtener la IP pública
az vm show -d \
  --resource-group "$RG" \
  --name "$VM" \
  --query publicIps \
  -o tsv
```

Mi recomendación es `Standard_B2ms` (2 vCPU, 8 GB): compila Angular, Maven y
PostgreSQL sin quedarse sin memoria. `Standard_B2s` (2 vCPU, 4 GB) es el mínimo
razonable y puede necesitar swap durante el primer build. Como la VM se usará
muy poco tiempo, la diferencia de costo entre ambas es de centavos. No uses
`B1s` ni `B1ms`: se quedan cortas de RAM para compilar.

> Seguridad: en el portal de Azure, restringe la regla SSH (puerto 22) a tu IP.
> No abras los puertos 8080 ni 5432.

## 2. Instalar Docker en la VM

```bash
ssh "$ADMIN_USER@IP_PUBLICA"

sudo apt-get update
sudo apt-get install -y ca-certificates curl git

curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker "$USER"

# Cerrar sesión y volver a entrar, o ejecutar:
newgrp docker

docker compose version
```

## 3. Clonar el repositorio

```bash
git clone --branch produccion https://github.com/AlexRax1/AnalisisSistema_Grupo_16_2026.git
cd AnalisisSistema_Grupo_16_2026
```

## 4. Configurar variables de entorno

```bash
cp .env.vm.example .env
chmod 600 .env
nano .env
```

Completa como mínimo:

```dotenv
DB_NAME=db_analisis_sistemas2
DB_USER=muni
DB_PASSWORD=<resultado de openssl rand -hex 24>

JWT_SECRET_KEY=<resultado de openssl rand -hex 48>
JWT_EXPIRATION_MS=1800000

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-correo@gmail.com
MAIL_PASSWORD=<App Password de Gmail, 16 caracteres sin espacios>

CORS_ALLOWED_ORIGINS=http://IP_PUBLICA_DE_LA_VM
HTTP_PORT=80
```

Genera los secretos así:

```bash
openssl rand -hex 24   # para DB_PASSWORD
openssl rand -hex 48   # para JWT_SECRET_KEY
```

Notas:

- Usa una **App Password** de Gmail, no la contraseña normal de la cuenta.
  Azure bloquea el puerto SMTP 25; por eso se usa el 587.
- Si cambias `HTTP_PORT`, abre ese mismo puerto en el NSG de la VM.
- No uses el carácter `$` en los valores del `.env`; los valores generados con
  `openssl rand -hex` no lo incluyen.
- El archivo `.env` no se debe subir a Git.
- `CORS_ALLOWED_ORIGINS` debe llevar la IP o dominio real, sin barra final.
  Aunque el frontend y el backend comparten host, se mantiene la lista por si
  se llama a la API directamente. Si cambias `HTTP_PORT`, incluye el puerto en
  el origen (por ejemplo, `http://20.1.2.3:8080`).

## 5. Construir y arrancar

```bash
docker compose up -d --build
```

La primera vez tarda varios minutos porque descarga imágenes, dependencias de
Maven/npm y compila Angular. Luego revisa:

```bash
docker compose ps
docker compose logs -f backend
```

Cuando todo esté sano:

```bash
curl http://localhost/actuator/health
```

Debe responder algo como:

```json
{"groups":["liveness","readiness"],"status":"UP"}
```

Abre en el navegador:

```text
http://IP_PUBLICA_DE_LA_VM/
```

Prueba el flujo completo: registrar un ciudadano, iniciar sesión, crear una
queja con una imagen y abrir la evidencia. Los módulos `/admin` siguen
deshabilitados hasta integrar la rama administrativa.

## 6. Operación diaria

```bash
# Ver estado
docker compose ps

# Ver logs
docker compose logs -f --tail=200

# Reiniciar un servicio
docker compose restart backend

# Detener sin borrar datos
docker compose stop

# Volver a arrancar
docker compose start
```

### Actualizar el código

```bash
git pull
docker compose up -d --build
```

Los volúmenes de PostgreSQL y evidencias se conservan entre actualizaciones.

### Reiniciar la base de datos y las evidencias

Esto **borra** los datos y vuelve a ejecutar el esquema y el seed:

```bash
docker compose down -v
docker compose up -d --build
```

Los scripts SQL se ejecutan únicamente cuando el volumen `postgres_data` está
vacío.

### Respaldos (opcional para la demo)

```bash
# Base de datos
docker compose exec -T db sh -c 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' \
  > backup-$(date +%F).sql

# Evidencias (volumen con el nombre del proyecto "muniques")
docker run --rm \
  -v muniques_evidencias_data:/data:ro \
  -v "$PWD":/backup \
  alpine tar czf /backup/evidencias-$(date +%F).tar.gz -C /data .
```

## 7. Apagar y eliminar

Para ahorrar costo sin borrar nada:

```bash
az vm deallocate --resource-group "$RG" --name "$VM"
```

> La IP pública Standard es estática y se conserva al hacer `deallocate`. Si
> usaras una IP dinámica, podría cambiar al volver a arrancar; en ese caso
> actualiza `CORS_ALLOWED_ORIGINS` en `.env` y ejecuta `docker compose up -d`.

Para eliminar todo al terminar la demo:

```bash
az group delete --name "$RG" --yes --no-wait
```

## 8. Seguridad y limitaciones de la demo

- La app se expone por **HTTP**, sin HTTPS. Las contraseñas y los JWT viajan
  sin cifrar. Usa únicamente datos de prueba y no credenciales reales.
- Si necesitas HTTPS, el siguiente paso es apuntar un dominio a la IP y poner
  Caddy delante de Nginx; no está incluido en esta guía.
- Las imágenes de `/evidencias` son accesibles para quien tenga la URL. Para
  datos sensibles habría que agregar autenticación a esa ruta.
- No publiques PostgreSQL ni el backend. La única entrada pública debe ser el
  puerto 80 de Nginx.
- Revoca la App Password de Gmail cuando termines la demo.
- El workflow de Static Web Apps quedó deshabilitado porque el despliegue
  actual es la VM. Si algún día vuelves a SWA, actívalo manualmente.

## 9. Problemas comunes

| Síntoma | Revisión |
| --- | --- |
| `db` no pasa a healthy | `docker compose logs db`; revisa `DB_NAME`, `DB_USER`, `DB_PASSWORD` |
| `backend` no pasa a healthy | `docker compose logs backend`; revisa `JWT_SECRET_KEY`, conexión a `db` y que el esquema se haya cargado |
| El esquema no existe | El volumen ya estaba creado; usa `docker compose down -v` y vuelve a arrancar |
| Error de Gmail | Debe ser App Password de 16 caracteres; la cuenta necesita 2FA |
| Error 413 al subir fotos | Nginx permite 25 MB; el backend limita cada archivo a 5 MB y 20 MB por petición |
| Recargar una ruta de Angular da 404 | Nginx debe tener el `try_files ... /index.html`; está en `frontend/nginx.conf` |
| La IP cambió | Actualiza `CORS_ALLOWED_ORIGINS` en `.env` y ejecuta `docker compose up -d` |
