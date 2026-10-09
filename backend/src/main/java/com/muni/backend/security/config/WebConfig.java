package com.muni.backend.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obtener la carpeta raíz del proyecto backend en ejecución
        String rutaProyecto = System.getProperty("user.dir");
        Path pathEvidencias = Paths.get(rutaProyecto, "subidas", "evidencias").toAbsolutePath().normalize();

        String uriDirectorio = pathEvidencias.toUri().toString();
        if (!uriDirectorio.endsWith("/")) {
            uriDirectorio += "/";
        }

        registry.addResourceHandler("/subidas/evidencias/**")
                .addResourceLocations(uriDirectorio);
    }
}
