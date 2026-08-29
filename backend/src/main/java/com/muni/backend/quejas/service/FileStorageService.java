package com.muni.backend.shared.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath = Paths.get("./subidas/evidencias");

    public FileStorageService() {
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de evidencias en disco", e);
        }
    }

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

        try {
            String extension = contentType.toLowerCase().contains("png") ? ".png" : ".jpg";
            String nombreArchivo = UUID.randomUUID() + extension;
            Path destino = uploadPath.resolve(nombreArchivo);

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return "/subidas/evidencias/" + nombreArchivo;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen en disco", e);
        }
    }
}