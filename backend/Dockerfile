# Etapa 1: Construcción del JAR con Maven y Java 21
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar archivos de Maven y dependencias
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente y compilar omitiendo tests para agilizar el build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen ligera de ejecución (JRE 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear directorio para almacenamiento de imágenes
RUN mkdir -p /app/subidas/evidencias

# Copiar el binario ejecutable
COPY --from=build /app/target/*.jar app.jar

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
