package com.muni.backend.quejas.model;

import com.muni.backend.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidencias_digitales")
@Data
public class EvidenciaDigital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evidencia_id")
    private Integer evidenciaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queja_id", nullable = false)
    private Queja queja;

    @Column(name = "etapa", length = 40)
    private String etapa = "REPORTE_CIUDADANO";

    @Column(name = "url_archivo", nullable = false, length = 300)
    private String urlArchivo;

    @Column(name = "nombre_archivo", length = 200)
    private String nombreArchivo;

    @Column(name = "formato", length = 10)
    private String formato;

    @Column(name = "tamanio_bytes")
    private Long tamanioBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subido_por_usuario_id")
    private Usuario subidoPor;

    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida = LocalDateTime.now();
}