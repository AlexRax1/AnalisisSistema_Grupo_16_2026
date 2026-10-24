package com.muni.backend.quejas.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice(basePackages = "com.muni.backend.quejas.controller")
public class ApiExceptionHandler {

    // Queja inexistente.
    @ExceptionHandler(QuejaNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse quejaNoEncontrada(QuejaNoEncontradaException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    // El usuario no tiene permiso sobre la queja.
    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse accesoDenegado(Exception ex) {
        return new ErrorResponse(ex.getMessage());
    }

    // La queja no está en el estado requerido.
    @ExceptionHandler(EstadoInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse estadoInvalido(EstadoInvalidoException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    // No hay tareas pendientes: se responde sin contenido.
    @ExceptionHandler(SinTareasDisponiblesException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sinTareasDisponibles() {
    }

    // Datos de entrada inválidos.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse argumentoInvalido(IllegalArgumentException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    // Errores de validación de los DTO.
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validacion(BindException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(texto -> texto != null && !texto.isBlank())
                .findFirst()
                .orElse("Datos de entrada inválidos.");
        return new ErrorResponse(mensaje);
    }

    // Falta una parte obligatoria del formulario multipart.
    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse parteFaltante(MissingServletRequestPartException ex) {
        return new ErrorResponse("Falta el campo obligatorio: " + ex.getRequestPartName());
    }

    // El cuerpo JSON no se pudo leer.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse cuerpoInvalido() {
        return new ErrorResponse("El cuerpo de la solicitud no es válido.");
    }

    // El tipo de un parámetro de la URL no coincide.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return new ErrorResponse("El parámetro '" + ex.getName() + "' no es válido.");
    }

    // El archivo supera el tamaño permitido.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorResponse archivoMuyGrande() {
        return new ErrorResponse("El archivo adjunto supera el tamaño máximo permitido.");
    }

    // Error no controlado: se registra y no se expone el detalle interno.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse errorInesperado(Exception ex) {
        log.error("Error no controlado en la API de quejas", ex);
        return new ErrorResponse("Ocurrió un error interno al procesar la solicitud.");
    }
}
