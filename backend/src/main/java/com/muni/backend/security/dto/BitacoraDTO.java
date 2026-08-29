package com.muni.backend.security.dto;
import lombok.Data;

@Data
public class BitacoraDTO {
    private Integer userId;
    private String microservicioAfectado;
    private String endpoint;
    private String accion;
    private String idAfectado;

}