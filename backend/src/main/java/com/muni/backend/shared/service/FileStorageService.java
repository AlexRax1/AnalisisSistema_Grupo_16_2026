package com.muni.backend.shared.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final Path uploadPath;

    public FileStorageService() {
        this.uploadPath = Paths.get("subidas/evidencias").toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de evidencias en disco: " + uploadPath, e);
        }
    }

    public Path getUploadPath() {
        return uploadPath;
    }

    // Devuelve la extensión lógica del archivo.
    public String obtenerFormato(String url) {
        if (url == null) {
            return "jpg";
        }
        int punto = url.lastIndexOf('.');
        if (punto < 0 || punto == url.length() - 1) {
            return "jpg";
        }
        return url.substring(punto + 1).toLowerCase();
    }

    public String guardarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo adjunto está vacío.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo adjunto excede el tamaño máximo permitido de 5 MB.");
        }

        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase()
                : "";

        String extension;
        if (contentType.equals("image/jpeg") || contentType.equals("image/jpg")) {
            extension = ".jpg";
        } else if (contentType.equals("image/png")) {
            extension = ".png";
        } else if (contentType.equals("image/webp")) {
            extension = ".webp";
        } else {
            throw new IllegalArgumentException(
                    "Solo se admiten archivos de imagen en formato JPG, JPEG, PNG o WEBP.");
        }

        if (!firmaValida(file, extension)) {
            throw new IllegalArgumentException(
                    "El archivo no es una imagen válida o está corrupto.");
        }

        try {
            String nombreArchivo = UUID.randomUUID() + extension;
            Path destino = uploadPath.resolve(nombreArchivo);
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return "/subidas/evidencias/" + nombreArchivo;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen en disco", e);
        }
    }

    // Verifica la firma real del archivo.
    private boolean firmaValida(MultipartFile file, String extension) {
        try (InputStream in = file.getInputStream()) {
            byte[] header = in.readNBytes(12);
            if (header.length < 4) {
                return false;
            }

            return switch (extension) {
                case ".jpg" -> header.length >= 3
                        && (header[0] & 0xFF) == 0xFF
                        && (header[1] & 0xFF) == 0xD8
                        && (header[2] & 0xFF) == 0xFF;
                case ".png" -> header.length >= 8
                        && (header[0] & 0xFF) == 0x89
                        && header[1] == 'P'
                        && header[2] == 'N'
                        && header[3] == 'G'
                        && header[4] == 0x0D
                        && header[5] == 0x0A
                        && header[6] == 0x1A
                        && header[7] == 0x0A;
                case ".webp" -> header.length >= 12
                        && header[0] == 'R'
                        && header[1] == 'I'
                        && header[2] == 'F'
                        && header[3] == 'F'
                        && header[8] == 'W'
                        && header[9] == 'E'
                        && header[10] == 'B'
                        && header[11] == 'P';
                default -> false;
            };
        } catch (IOException e) {
            return false;
        }
    }
}
