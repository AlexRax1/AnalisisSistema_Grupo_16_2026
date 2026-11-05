package com.muni.backend.bitacora;

import com.muni.backend.bitacora.dto.BitacoraFiltroDTO;
import com.muni.backend.bitacora.dto.BitacoraRespuestaDTO;
import com.muni.backend.bitacora.model.Bitacora;
import com.muni.backend.bitacora.repository.BitacoraRepository;
import com.muni.backend.bitacora.service.BitacoraService;
import com.muni.backend.bitacora.service.ClientIpService;
import com.muni.backend.security.model.Credencial;
import com.muni.backend.security.repository.CredencialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BitacoraServiceTest {

    @Mock
    private BitacoraRepository bitacoraRepository;

    @Mock
    private CredencialRepository credencialRepository;

    @Mock
    private ClientIpService clientIpService;

    @InjectMocks
    private BitacoraService bitacoraService;

    @Test
    void debeGuardarIpUsuarioYDescripcionEnCadaRegistro() {
        Credencial admin = new Credencial();
        admin.setUserId(7);
        admin.setUsername("admin");

        when(clientIpService.obtenerIp()).thenReturn("203.0.113.10");
        when(credencialRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        bitacoraService.registrar(
                "admin",
                "usuarios",
                "1",
                "estado",
                "ACTUALIZAR",
                "INACTIVO",
                "ACTIVO"
        );

        ArgumentCaptor<Bitacora> captor = ArgumentCaptor.forClass(Bitacora.class);
        verify(bitacoraRepository).save(captor.capture());

        Bitacora guardada = captor.getValue();
        assertEquals("203.0.113.10", guardada.getDireccionIp());
        assertEquals("admin", guardada.getUsuarioAccion());
        assertEquals(7, guardada.getUserId());
        assertTrue(guardada.getDescripcion().contains("ACTUALIZAR en usuarios (estado)"));
        assertTrue(guardada.getDescripcion().contains("INACTIVO -> ACTIVO"));
    }

    @Test
    void debeUsarSistemaCuandoNoHayUsuarioNiToken() {
        when(clientIpService.obtenerIp()).thenReturn("127.0.0.1");
        when(credencialRepository.findByUsernameIgnoreCase("SISTEMA")).thenReturn(Optional.empty());

        bitacoraService.registrar("credenciales", "acceso", "LOGIN", null, "Evento del sistema");

        ArgumentCaptor<Bitacora> captor = ArgumentCaptor.forClass(Bitacora.class);
        verify(bitacoraRepository).save(captor.capture());
        assertEquals("SISTEMA", captor.getValue().getUsuarioAccion());
        assertTrue(captor.getValue().getDescripcion().contains("Evento del sistema"));
    }

    @Test
    void debeConsultarLaBitacoraConFiltrosYPaginacion() {
        Bitacora registro = new Bitacora();
        registro.setRegistroId(1);
        registro.setUsuarioAccion("admin");
        registro.setTablaAfectada("credenciales");
        registro.setAccion("LOGIN");
        registro.setDescripcion("LOGIN en credenciales (acceso)");

        Page<Bitacora> pagina = new PageImpl<>(List.of(registro));
        when(bitacoraRepository.buscarConFiltros(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(pagina);

        BitacoraFiltroDTO filtro = new BitacoraFiltroDTO();
        filtro.setUsuario(" admin ");

        Page<BitacoraRespuestaDTO> resultado = bitacoraService.listar(
                filtro, Pageable.unpaged());

        assertEquals(1, resultado.getTotalElements());
        assertEquals("admin", resultado.getContent().get(0).getUsuarioAccion());
        assertEquals("LOGIN", resultado.getContent().get(0).getAccion());
        assertEquals("LOGIN en credenciales (acceso)", resultado.getContent().get(0).getDescripcion());
    }

    @Test
    void debeFiltrarPorIdAfectadoYCampoAfectado() {
        when(bitacoraRepository.buscarConFiltros(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        BitacoraFiltroDTO filtro = new BitacoraFiltroDTO();
        filtro.setIdAfectado("13");
        filtro.setCampoAfectado("estado_actual");

        bitacoraService.listar(filtro, Pageable.unpaged());

        verify(bitacoraRepository).buscarConFiltros(
                isNull(),
                isNull(),
                isNull(),
                eq("13"),
                eq("estado_actual"),
                any(),
                any(),
                any(Pageable.class)
        );
    }
}
