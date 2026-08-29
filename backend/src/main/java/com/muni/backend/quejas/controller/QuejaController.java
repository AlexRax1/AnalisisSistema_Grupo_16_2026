package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.QuejaRegistroDTO;
import com.muni.backend.quejas.dto.QuejaRegistroResponse;
import com.muni.backend.quejas.service.QuejaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/quejas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class QuejaController {

    private final QuejaService quejaService;

    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrarQueja(
            @RequestPart("datos") QuejaRegistroDTO dto,
            @RequestPart("fotos") List<MultipartFile> fotos,
            Authentication authentication
    ) {
        try {
            String correoUsuario = authentication.getName();
            QuejaRegistroResponse response = quejaService.registrarQueja(dto, fotos, correoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el registro: " + e.getMessage());
        }
    }
}