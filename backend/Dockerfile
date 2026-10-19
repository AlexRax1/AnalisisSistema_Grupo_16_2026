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

# Copiar el binario ejecutable
COPY --from=build /app/target/*.jar app.jar

# Puerto y opciones de JVM para contenedor (75 % de la memoria del contenedor)
ENV SERVER_PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# exec deja a Java como PID 1 para recibir correctamente SIGTERM
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
