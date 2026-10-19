# Despliegue rápido en Azure

Guía para la demo de 3 días. Ejecutar desde la raíz del repositorio con Azure CLI, `psql`, `openssl` y Node.js 20+ instalados.

## 0. Variables

Cambiar los nombres si ya están ocupados. Los nombres de ACR, Storage y PostgreSQL deben ser únicos en Azure.

```bash
set -euo pipefail

export RG="rg-muniques-demo"
export LOCATION="eastus2"
export ACR_NAME="acrmuniquesg16"
export IMAGE_NAME="muniques-backend"
export IMAGE_TAG="v1"
export CONTAINER_ENV="cae-muniques-demo"
export APP_NAME="ca-muniques-api"
export DB_SERVER="pg-muniques-g16"
export DB_NAME="db_analisis_sistemas2"
export DB_ADMIN="pgadminmuni"
export DB_PASSWORD='CAMBIAR_PASSWORD_SEGURA'
export STORAGE_ACCOUNT="stmuniquesg16"
export STORAGE_CONTAINER="evidencias"
export SWA_NAME="swa-muniques-demo"
export MAIL_USERNAME="tu-correo@gmail.com"
# App Password de Gmail (16 caracteres, sin espacios), no la contraseña normal.
export MAIL_PASSWORD='APP_PASSWORD_DE_GMAIL'
JWT_SECRET="$(openssl rand -base64 48)"
export JWT_SECRET
```

## 1. Crear recursos base

```bash
az login
# Si hay varias suscripciones:
# az account set --subscription "<SUSCRIPCION>"

az group create --name "$RG" --location "$LOCATION"

az acr create \
  --resource-group "$RG" \
  --name "$ACR_NAME" \
  --sku Basic \
  --admin-enabled true

az storage account create \
  --resource-group "$RG" \
  --name "$STORAGE_ACCOUNT" \
  --location "$LOCATION" \
  --sku Standard_LRS \
  --kind StorageV2 \
  --allow-blob-public-access true

STORAGE_CONNECTION_STRING=$(az storage account show-connection-string \
  --resource-group "$RG" \
  --name "$STORAGE_ACCOUNT" \
  --query connectionString \
  -o tsv)
export STORAGE_CONNECTION_STRING

az storage container create \
  --name "$STORAGE_CONTAINER" \
  --connection-string "$STORAGE_CONNECTION_STRING" \
  --public-access blob
```

El contenedor es de lectura pública: subir únicamente evidencias de prueba.

PostgreSQL Flexible Server (acceso público solo para servicios de Azure; se agrega temporalmente la IP local para cargar el SQL):

```bash
az postgres flexible-server create \
  --resource-group "$RG" \
  --name "$DB_SERVER" \
  --location "$LOCATION" \
  --admin-user "$DB_ADMIN" \
  --admin-password "$DB_PASSWORD" \
  --version 16 \
  --tier Burstable \
  --sku-name Standard_B1ms \
  --storage-size 32 \
  --public-access 0.0.0.0 \
  --yes

az postgres flexible-server db create \
  --resource-group "$RG" \
  --server-name "$DB_SERVER" \
  --name "$DB_NAME"

MY_IP=$(curl -fsS https://api.ipify.org)
az postgres flexible-server firewall-rule create \
  --resource-group "$RG" \
  --server-name "$DB_SERVER" \
  --name "allow-local" \
  --start-ip-address "$MY_IP" \
  --end-ip-address "$MY_IP"
```

## 2. Cargar esquema y usuarios de prueba

```bash
PGPASSWORD="$DB_PASSWORD" psql "host=$DB_SERVER.postgres.database.azure.com port=5432 dbname=$DB_NAME user=$DB_ADMIN sslmode=require" \
  -f sql/db_MuniQuejas.sql
PGPASSWORD="$DB_PASSWORD" psql "host=$DB_SERVER.postgres.database.azure.com port=5432 dbname=$DB_NAME user=$DB_ADMIN sslmode=require" \
  -f sql/seed_usuarios.sql
```

Se puede eliminar la regla temporal cuando termine la carga:

```bash
az postgres flexible-server firewall-rule delete \
  --resource-group "$RG" \
  --server-name "$DB_SERVER" \
  --name "allow-local" \
  --yes
```

## 3. Construir la imagen en ACR

```bash
az acr build \
  --registry "$ACR_NAME" \
  --image "$IMAGE_NAME:$IMAGE_TAG" \
  backend
```

## 4. Crear Container App

Primero se crea la Static Web App para conocer su hostname y configurarlo en CORS:

```bash
az staticwebapp create \
  --name "$SWA_NAME" \
  --resource-group "$RG" \
  --sku Free

SWA_HOST=$(az staticwebapp show \
  --name "$SWA_NAME" \
  --resource-group "$RG" \
  --query defaultHostname \
  -o tsv)
export SWA_HOST
```

Luego el entorno y la aplicación:

```bash
az containerapp env create \
  --name "$CONTAINER_ENV" \
  --resource-group "$RG" \
  --location "$LOCATION"

ACR_USERNAME=$(az acr credential show --name "$ACR_NAME" --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name "$ACR_NAME" --query "passwords[0].value" -o tsv)

BACKEND_FQDN=$(az containerapp create \
  --name "$APP_NAME" \
  --resource-group "$RG" \
  --environment "$CONTAINER_ENV" \
  --image "$ACR_NAME.azurecr.io/$IMAGE_NAME:$IMAGE_TAG" \
  --registry-server "$ACR_NAME.azurecr.io" \
  --registry-username "$ACR_USERNAME" \
  --registry-password "$ACR_PASSWORD" \
  --ingress external \
  --target-port 8080 \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 0 \
  --max-replicas 1 \
  --secrets \
    db-password="$DB_PASSWORD" \
    jwt-secret="$JWT_SECRET" \
    mail-password="$MAIL_PASSWORD" \
    storage-connection-string="$STORAGE_CONNECTION_STRING" \
  --env-vars \
    SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    DB_HOST="$DB_SERVER.postgres.database.azure.com" \
    DB_PORT=5432 \
    DB_NAME="$DB_NAME" \
    DB_USER="$DB_ADMIN" \
    DB_PASSWORD=secretref:db-password \
    JWT_SECRET_KEY=secretref:jwt-secret \
    JWT_EXPIRATION_MS=1800000 \
    MAIL_HOST=smtp.gmail.com \
    MAIL_PORT=587 \
    MAIL_USERNAME="$MAIL_USERNAME" \
    MAIL_PASSWORD=secretref:mail-password \
    CORS_ALLOWED_ORIGINS="https://$SWA_HOST" \
    AZURE_STORAGE_CONNECTION_STRING=secretref:storage-connection-string \
    AZURE_STORAGE_CONTAINER="$STORAGE_CONTAINER" \
  --query "properties.configuration.ingress.fqdn" \
  -o tsv)
export BACKEND_FQDN

echo "Backend: https://$BACKEND_FQDN"
# Esperar a que arranque la primera réplica (puede tardar si está en cero).
for i in $(seq 1 30); do
  curl -fsS "https://$BACKEND_FQDN/actuator/health" >/dev/null && break
  sleep 5
done
curl -fsS "https://$BACKEND_FQDN/actuator/health"
```

La primera petición puede tardar si la réplica estuvo en cero.

## 5. Compilar y desplegar el frontend

Azure CLI no publica contenido en Static Web Apps; se usa la CLI oficial `@azure/static-web-apps-cli` con el token de despliegue.

> Si en su lugar usas el workflow de GitHub Actions generado por Azure, configura
> `app_location: frontend`, `api_location: ""`,
> `output_location: dist/municipalidadFrontend/browser` y
> `app_build_command: "BACKEND_URL=${{ vars.BACKEND_URL }} npm run build"`.
> Define la variable de repositorio `BACKEND_URL` con la URL real del backend.
> El error `failed to produce artifact folder: 'APP_NAME'` significa que
> `output_location` quedó con el placeholder en lugar de la ruta del build.

```bash
cd frontend
npm ci
BACKEND_URL="https://$BACKEND_FQDN" npm run build

SWA_TOKEN=$(az staticwebapp secrets list \
  --name "$SWA_NAME" \
  --resource-group "$RG" \
  --query "properties.apiKey" \
  -o tsv)

npx --yes @azure/static-web-apps-cli deploy \
  dist/municipalidadFrontend/browser \
  --deployment-token "$SWA_TOKEN" \
  --env production
cd ..

echo "Frontend: https://$SWA_HOST"
```

`staticwebapp.config.json` ya se copia al build y resuelve las rutas del SPA.

## 6. Teardown al finalizar la demo

Exportar cualquier dato necesario antes de ejecutar:

```bash
az group delete --name "$RG" --yes --no-wait
```

Esto elimina el grupo completo: Container Apps, PostgreSQL, Blob Storage, ACR, Static Web App y Log Analytics. No se puede deshacer.
