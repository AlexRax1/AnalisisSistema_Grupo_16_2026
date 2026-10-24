package com.muni.backend.bitacora.controller;

import com.muni.backend.bitacora.dto.BitacoraDTO;
import com.muni.backend.bitacora.service.BitacoraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Endpoint manual de bitácora para administradores.
@RestController
@RequestMapping("/admin/bitacora")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BitacoraController {

    private final BitacoraService bitacoraService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody BitacoraDTO dto) {
        bitacoraService.registrar(
                dto.getUserId(),
                dto.getUsuarioAccion(),
                dto.getTablaAfectada(),
                dto.getIdAfectado(),
                dto.getCampoAfectado(),
                dto.getAccion(),
                dto.getRegistroAntiguo(),
                dto.getRegistroNuevo()
        );
        return ResponseEntity.ok().build();
    }
}
