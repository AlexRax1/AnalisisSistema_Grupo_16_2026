package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.shared.EmpleadoDTO;
import com.muni.backend.quejas.dto.shared.GuardarEmpleadoDTO;
import com.muni.backend.usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/usuarios-internos")
@CrossOrigin(origins = "*")
public class AdminUsuarioController {

    @Autowired
    private UsuarioService usuarioService; // Reemplaza por el nombre real de tu Service

    // 1. GET: Listar todos los empleados/usuarios internos
    @GetMapping
    public ResponseEntity<List<EmpleadoDTO>> listarUsuarios() {
        List<EmpleadoDTO> usuarios = usuarioService.listarUsuariosInternos();
        return ResponseEntity.ok(usuarios);
    }

    // 2. POST: Registrar un nuevo empleado
    @PostMapping
    public ResponseEntity<?> registrarEmpleado(@RequestBody GuardarEmpleadoDTO dto) {
        try {
            usuarioService.registrarEmpleado(dto);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Empleado registrado exitosamente.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 3. PUT: Actualizar datos de un empleado existente
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEmpleado(@PathVariable Long id, @RequestBody GuardarEmpleadoDTO dto) {
        try {
            usuarioService.actualizarEmpleado(id, dto);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Empleado actualizado exitosamente.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 4. PUT: Alternar/Cambiar estado (activo/inactivo)
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        try {
            Boolean activo = payload.get("activo");
            if (activo == null) {
                activo = false;
            }

            usuarioService.cambiarEstadoEmpleado(id, activo);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Estado del empleado actualizado correctamente.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Error al cambiar estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
