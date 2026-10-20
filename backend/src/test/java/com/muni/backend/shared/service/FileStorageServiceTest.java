package com.muni.backend.shared.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void guardaArchivoEnDiscoYDevuelveRutaRelativa() throws Exception {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "storageType", "LOCAL");
        ReflectionTestUtils.setField(service, "storagePath", tempDir.toString());

        MockMultipartFile archivo = new MockMultipartFile(
                "fotos",
                "evidencia.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4});

        String url = service.guardarArchivo(archivo);

        assertThat(url).startsWith("/evidencias/").endsWith(".jpg");
        Path archivoGuardado = tempDir.resolve(url.substring("/evidencias/".length()));
        assertThat(archivoGuardado).exists();
        assertThat(Files.readAllBytes(archivoGuardado)).containsExactly(1, 2, 3, 4);
    }

    @Test
    void rechazaArchivosQueNoSonImagen() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "storageType", "local");
        ReflectionTestUtils.setField(service, "storagePath", tempDir.toString());

        MockMultipartFile archivo = new MockMultipartFile(
                "fotos",
                "documento.pdf",
                "application/pdf",
                new byte[]{1, 2, 3});

        assertThatThrownBy(() -> service.guardarArchivo(archivo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JPG");
    }
}
