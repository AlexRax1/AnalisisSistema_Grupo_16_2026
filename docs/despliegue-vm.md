# Despliegue en una VM de Azure con Docker Compose

Esta guía levanta todo el sistema en una sola VM: PostgreSQL, backend Spring
Boot y frontend Angular servido por Nginx. Nginx también hace de reverse proxy
hacia el backend, por lo que el navegador usa el mismo origen y no hay CORS
entre frontend y API.

```text
Internet
   |
   | puerto 80
   v
Nginx (contenedor frontend)
   |-- sirve Angular
   |-- /auth, /quejas, /usuarios, /admin, /api, /subidas, /actuator -> backend
                                                                          |
                                                                          v
                                                                    PostgreSQL
```

## 1. Crear la VM

Requisitos: Azure CLI instalado y sesión iniciada con `az login`.

```bash
az group create --name rg-muniques --location eastus
az vm create \
  --resource-group rg-muniques \
  --name vm-muniques \
  --image Ubuntu2204 \
  --size Standard_B2s \
  --admin-username azureuser \
  --generate-ssh-keys

az vm open-port --resource-group rg-muniques --name vm-muniques --port 80 --priority 1010
```

Guarda la IP pública que devuelve el comando. Solo hacen falta los puertos 22
(SSH) y 80 (HTTP). El 443 se abre cuando se configure HTTPS.

## 2. Instalar Docker en la VM

```bash
ssh azureuser@IP_PUBLICA

sudo apt-get update
sudo apt-get install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker "$USER"
newgrp docker
```

## 3. Clonar y configurar

```bash
git clone https://github.com/AlexRax1/AnalisisSistema_Grupo_16_2026.git
cd AnalisisSistema_Grupo_16_2026
cp .env.vm.example .env
nano .env
```

Cambia como mínimo:

- `DB_PASSWORD`
- `JWT_SECRET_KEY` (genera uno con `openssl rand -base64 48`)
- `CORS_ALLOWED_ORIGINS` con la IP o dominio real, por ejemplo
  `http://20.10.30.40`

El correo es opcional para la demo. Si no se configura, la recuperación de
contraseña por correo no funcionará, pero el resto del sistema sí.

## 4. Levantar el sistema

```bash
docker compose up -d --build
docker compose ps
curl http://localhost/actuator/health
```

La primera construcción tarda varios minutos porque descarga dependencias de
Maven y npm. La base de datos se inicializa sola la primera vez con
`sql/db_MuniQuejas.sql` y `sql/seed_usuarios.sql`.

Abre en el navegador:

```text
http://IP_PUBLICA
```

## 5. Usuarios de demostración

La contraseña inicial de todos es `Muni123456`.

| Rol | Usuario |
| --- | --- |
| Administrador | `admin@municipalidad.local` |
| Funcionario municipal | `funcionario@municipalidad.local` |
| Inspector de campo | `inspector@municipalidad.local` |
| Especialista técnico | `especialista@municipalidad.local` |
| Ciudadano | `ciudadano@municipalidad.local` |

Cambia estas contraseñas antes de usar el sistema con datos reales. El seed
está pensado para una demo, no para producción abierta a Internet.

## 6. Operación básica

```bash
# Ver logs
docker compose logs -f backend
docker compose logs -f frontend

# Reiniciar solo el backend
docker compose restart backend

# Actualizar código
git pull
docker compose up -d --build

# Detener sin borrar datos
docker compose down
```

Los datos de PostgreSQL y las evidencias subidas quedan en volúmenes Docker:

- `postgres_data`
- `evidencias_data`

Para borrar todo y reinicializar la base desde cero:

```bash
docker compose down -v
```

Ese comando elimina la base y las evidencias. Úsalo solo en una demo.

## 7. Notas de seguridad y pendientes

- El sistema queda por HTTP. Para un dominio real, configura HTTPS con
  Certbot/Caddy o un Application Gateway delante de la VM.
- No abras PostgreSQL (5432) a Internet. Solo Nginx debe ser público.
- El backend valida el esquema con `ddl-auto: validate` en el perfil `prod`.
  Si cambias entidades, actualiza también `sql/db_MuniQuejas.sql`.
- Las imágenes de evidencia se sirven mediante `/subidas/**` a través de Nginx.
- El correo se configura en `.env`; sin credenciales válidas, la recuperación
  de contraseña no envía mensajes.
