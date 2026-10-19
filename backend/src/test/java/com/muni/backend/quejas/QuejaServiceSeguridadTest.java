package com.muni.backend.quejas;

import com.muni.backend.quejas.exception.AccesoDenegadoException;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.repository.EvidenciaDigitalRepository;
import com.muni.backend.quejas.repository.HistorialEstadoQuejaRepository;
import com.muni.backend.quejas.repository.QuejaRepository;
import com.muni.backend.quejas.service.AsignacionAutomaticaService;
import com.muni.backend.quejas.service.QuejaService;
import com.muni.backend.shared.service.FileStorageService;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuejaServiceSeguridadTest {

    @Mock
    private QuejaRepository quejaRepository;
    @Mock
    private EvidenciaDigitalRepository evidenciaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private HistorialEstadoQuejaRepository historialRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private AsignacionAutomaticaService asignacionAutomaticaService;

    @InjectMocks
    private QuejaService quejaService;

    @Test
    void deniegaElDetalleDeUnaQuejaAjena() {
        Usuario propietario = usuario(1);
        Usuario otroCiudadano = usuario(2);
        Queja queja = quejaDe(propietario);

        when(quejaRepository.findByCorrelativo("Q-2026-0001")).thenReturn(Optional.of(queja));
        when(usuarioRepository.findByCredencial_Username("otro@test.com"))
                .thenReturn(Optional.of(otroCiudadano));

        assertThrows(AccesoDenegadoException.class,
                () -> quejaService.obtenerPorCorrelativo("Q-2026-0001", "otro@test.com"));
    }

    @Test
    void permiteElDetalleAlPropietario() {
        Usuario propietario = usuario(1);
        Queja queja = quejaDe(propietario);

        when(quejaRepository.findByCorrelativo("Q-2026-0001")).thenReturn(Optional.of(queja));
        when(usuarioRepository.findByCredencial_Username("dueno@test.com"))
                .thenReturn(Optional.of(propietario));
        when(evidenciaRepository.findByQueja(queja)).thenReturn(List.of());

        assertDoesNotThrow(
                () -> quejaService.obtenerPorCorrelativo("Q-2026-0001", "dueno@test.com"));
    }

    private Usuario usuario(Integer usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setUsuarioId(usuarioId);
        return usuario;
    }

    private Queja quejaDe(Usuario ciudadano) {
        Queja queja = new Queja();
        queja.setCorrelativo("Q-2026-0001");
        queja.setCiudadano(ciudadano);
        return queja;
    }
}
