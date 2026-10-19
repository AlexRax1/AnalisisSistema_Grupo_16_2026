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
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String CONTENEDOR_POR_DEFECTO = "evidencias";

    @Value("${AZURE_STORAGE_CONNECTION_STRING:}")
    private String connectionString;

    @Value("${AZURE_STORAGE_CONTAINER:evidencias}")
    private String containerName;

    private volatile BlobContainerClient containerClient;

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

        if (file.getSize() > 5242880) {
            throw new IllegalArgumentException("El archivo adjunto excede el tamaño máximo permitido de 5 MB.");
        }

        String extension = contentType.toLowerCase().contains("png") ? ".png" : ".jpg";
        String contentTypeFinal = extension.equals(".png") ? "image/png" : "image/jpeg";
        String nombreArchivo = UUID.randomUUID() + extension;

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
