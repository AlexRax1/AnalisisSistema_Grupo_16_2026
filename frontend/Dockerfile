# syntax=docker/dockerfile:1

# Etapa 1: compilar Angular en modo producción.
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .

# En la VM el frontend y el backend comparten host: Nginx hace de reverse proxy.
ARG BACKEND_URL=same-origin
RUN BACKEND_URL="$BACKEND_URL" npm run build

# Etapa 2: servir los archivos estáticos y proxy hacia el backend.
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/municipalidadFrontend/browser /usr/share/nginx/html

EXPOSE 80
