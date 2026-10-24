package com.muni.backend.shared.config;

import com.muni.backend.shared.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorageService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = fileStorageService.getUploadPath();
        String uriString = uploadDir.toUri().toString();
        if (!uriString.endsWith("/")) {
            uriString += "/";
        }

        registry.addResourceHandler("/subidas/evidencias/**")
                .addResourceLocations(uriString);
    }
}
