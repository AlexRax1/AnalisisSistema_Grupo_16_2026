package com.muni.backend.bitacora;

import com.muni.backend.bitacora.model.Bitacora;
import com.muni.backend.bitacora.repository.BitacoraRepository;
import com.muni.backend.bitacora.service.BitacoraService;
import com.muni.backend.bitacora.service.ClientIpService;
import com.muni.backend.security.repository.CredencialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void debeGuardarLaDireccionIpEnCadaRegistro() {
        when(clientIpService.obtenerIp()).thenReturn("203.0.113.10");
        when(credencialRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.empty());

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
        assertEquals("203.0.113.10", captor.getValue().getDireccionIp());
        assertEquals("admin", captor.getValue().getUsuarioAccion());
    }
}
