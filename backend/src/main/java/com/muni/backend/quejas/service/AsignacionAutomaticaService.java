package com.muni.backend.quejas.service;

import com.muni.backend.quejas.exception.EstadoInvalidoException;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsignacionAutomaticaService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene el Funcionario Municipal activo con menor número de quejas activas.
     * Retorna Optional por si aún no se han configurado funcionarios en el sistema.
     */
    public Optional<Usuario> asignarFuncionarioAutomatico() {
        List<Integer> ids = usuarioRepository.buscarIdFuncionarioMenorCarga(PageRequest.of(0, 1));
        if (ids.isEmpty()) {
            log.warn("No se encontraron funcionarios municipales activos en el sistema para asignación automática.");
            return Optional.empty();
        }
        return usuarioRepository.findById(ids.get(0));
    }

    /**
     * Obtiene el Inspector de Campo activo con menor número de quejas en inspección.
     * Lanza excepción si no hay inspectores activos disponibles.
     */
    public Usuario asignarInspectorAutomatico() {
        List<Integer> ids = usuarioRepository.buscarIdInspectorMenorCarga(PageRequest.of(0, 1));
        if (ids.isEmpty()) {
            throw new EstadoInvalidoException(
                "No hay inspectores de campo activos disponibles en el sistema para realizar la asignación automática.");
        }
        return usuarioRepository.findById(ids.get(0))
                .orElseThrow(() -> new EstadoInvalidoException("Inspector asignado no encontrado."));
    }

    /**
     * Obtiene el Especialista Técnico activo con menor número de quejas en reparación.
     * Prioriza especialistas de la dependencia indicada. Si no hay en esa dependencia, busca a nivel global.
     */
    public Usuario asignarEspecialistaAutomatico(Integer dependenciaId) {
        if (dependenciaId != null) {
            List<Integer> idsDep = usuarioRepository.buscarIdEspecialistaDependenciaMenorCarga(
                    dependenciaId, PageRequest.of(0, 1));
            if (!idsDep.isEmpty()) {
                return usuarioRepository.findById(idsDep.get(0))
                        .orElseThrow(() -> new EstadoInvalidoException("Especialista asignado no encontrado."));
            }
            log.info("No hay especialistas activos en la dependencia {}, buscando especialista a nivel global...", dependenciaId);
        }

        List<Integer> idsGlobal = usuarioRepository.buscarIdEspecialistaGlobalMenorCarga(PageRequest.of(0, 1));
        if (idsGlobal.isEmpty()) {
            throw new EstadoInvalidoException(
                "No hay especialistas técnicos activos disponibles en el sistema para realizar la asignación automática.");
        }
        return usuarioRepository.findById(idsGlobal.get(0))
                .orElseThrow(() -> new EstadoInvalidoException("Especialista asignado no encontrado."));
    }
}
