package com.muni.backend.quejas.exception;

public class QuejaNoEncontradaException extends RuntimeException {
    public QuejaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
