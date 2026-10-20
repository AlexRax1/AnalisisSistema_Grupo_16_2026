package com.muni.backend.shared.service;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String CONTENEDOR_POR_DEFECTO = "evidencias";
    private static final long TAMANIO_MAXIMO_BYTES = 5L * 1024 * 1024;

    @Value("${AZURE_STORAGE_CONNECTION_STRING:}")
    private String connectionString;

    @Value("${AZURE_STORAGE_CONTAINER:evidencias}")
    private String containerName;

    @Value("${STORAGE_TYPE:azure}")
    private String storageType;

    @Value("${STORAGE_PATH:evidencias}")
    private String storagePath;

    private volatile BlobContainerClient containerClient;

    /**
     * Guarda una evidencia en Azure Blob Storage o en un volumen local.
     *
     * El modo se elige con STORAGE_TYPE=azure|local. En local se devuelve una
     * ruta relativa (/evidencias/...) que Nginx sirve desde el volumen compartido.
     */
    public String guardarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo adjunto está vacío.");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("image/jpeg")
                && !contentType.equalsIgnoreCase("image/jpg")
                && !contentType.equalsIgnoreCase("image/png"))) {
            throw new IllegalArgumentException("Solo se admiten archivos de imagen en formato JPG, JPEG o PNG.");
        }

        if (file.getSize() > TAMANIO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("El archivo adjunto excede el tamaño máximo permitido de 5 MB.");
        }

        String tipoAlmacenamiento = storageType == null
                ? "azure"
                : storageType.trim().toLowerCase(Locale.ROOT);

        String extension = contentType.toLowerCase(Locale.ROOT).contains("png") ? ".png" : ".jpg";
        String contentTypeFinal = extension.equals(".png") ? "image/png" : "image/jpeg";
        String nombreArchivo = UUID.randomUUID() + extension;

        if ("local".equals(tipoAlmacenamiento)) {
            return guardarLocal(file, nombreArchivo);
        }

        if ("azure".equals(tipoAlmacenamiento) || tipoAlmacenamiento.isBlank()) {
            return guardarAzure(file, nombreArchivo, contentTypeFinal);
        }

        throw new IllegalStateException(
                "STORAGE_TYPE inválido: '" + storageType + "'. Use 'local' o 'azure'.");
    }

    private String guardarLocal(MultipartFile file, String nombreArchivo) {
        String rutaConfigurada = storagePath == null || storagePath.isBlank()
                ? "evidencias"
                : storagePath.trim();

        try {
            Path directorio = Paths.get(rutaConfigurada).toAbsolutePath().normalize();
            Files.createDirectories(directorio);

            Path destino = directorio.resolve(nombreArchivo).normalize();
            if (!destino.startsWith(directorio)) {
                throw new IllegalArgumentException("Ruta de archivo inválida.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/evidencias/" + nombreArchivo;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen en el almacenamiento local", e);
        }
    }

    private String guardarAzure(MultipartFile file, String nombreArchivo, String contentTypeFinal) {
        BlobContainerClient container = obtenerContenedor();

        try (InputStream inputStream = file.getInputStream()) {
            BlobClient blobClient = container.getBlobClient(nombreArchivo);
            blobClient.upload(BinaryData.fromStream(inputStream), true);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentTypeFinal));
            return blobClient.getBlobUrl();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer la imagen para subirla a Azure Blob Storage", e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al subir la imagen a Azure Blob Storage", e);
        }
    }

    private BlobContainerClient obtenerContenedor() {
        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException(
                    "No se configuró la variable de entorno AZURE_STORAGE_CONNECTION_STRING.");
        }

        BlobContainerClient client = containerClient;
        if (client == null) {
            synchronized (this) {
                client = containerClient;
                if (client == null) {
                    String nombreContenedor = containerName == null || containerName.isBlank()
                            ? CONTENEDOR_POR_DEFECTO
                            : containerName.trim();
                    client = new BlobServiceClientBuilder()
                            .connectionString(connectionString.trim())
                            .buildClient()
                            .getBlobContainerClient(nombreContenedor);
                    containerClient = client;
                }
            }
        }
        return client;
    }
}
