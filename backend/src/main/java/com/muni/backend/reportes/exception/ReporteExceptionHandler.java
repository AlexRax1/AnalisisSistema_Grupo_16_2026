package com.muni.backend.reportes.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.muni.backend.reportes.controller")
public class ReporteExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> argumentoInvalido(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Solicitud inválida.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> cuerpoInvalido() {
        return Map.of("error", "El cuerpo de la solicitud no es válido.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> errorInesperado(Exception ex) {
        log.error("Error no controlado en la API de reportes", ex);
        return Map.of("error", "Ocurrió un error interno al procesar el reporte.");
    }
}
