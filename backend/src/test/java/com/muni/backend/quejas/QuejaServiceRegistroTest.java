package com.muni.backend.quejas;

import com.muni.backend.bitacora.service.BitacoraService;
import com.muni.backend.quejas.dto.ciudadano.QuejaRegistroDTO;
import com.muni.backend.quejas.model.CategoriaQueja;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.model.SubcategoriaQueja;
import com.muni.backend.quejas.repository.EvidenciaDigitalRepository;
import com.muni.backend.quejas.repository.HistorialEstadoQuejaRepository;
import com.muni.backend.quejas.repository.QuejaRepository;
import com.muni.backend.quejas.service.AsignacionAutomaticaService;
import com.muni.backend.quejas.service.CatalogoService;
import com.muni.backend.quejas.service.QuejaService;
import com.muni.backend.security.model.Credencial;
import com.muni.backend.shared.service.FileStorageService;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuejaServiceRegistroTest {

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

    @Mock
    private CatalogoService catalogoService;

    @Mock
    private BitacoraService bitacoraService;

    @InjectMocks
    private QuejaService quejaService;

    @Test
    void registrarQuejaDebeGuardarCategoriaYSubcategoriaComoAsociaciones() {
        CategoriaQueja categoria = new CategoriaQueja();
        categoria.setCategoriaId(1);
        categoria.setNombreCategoria("Alumbrado Público");

        SubcategoriaQueja subcategoria = new SubcategoriaQueja();
        subcategoria.setSubcategoriaId(1);
        subcategoria.setCategoria(categoria);
        subcategoria.setNombreSubcategoria("Luminaria apagada / quemada");

        Credencial credencial = new Credencial();
        credencial.setUsername("ciudadano@test.com");

        Usuario ciudadano = new Usuario();
        ciudadano.setUsuarioId(3);
        ciudadano.setCorreo("ciudadano@test.com");
        ciudadano.setCredencial(credencial);

        QuejaRegistroDTO dto = new QuejaRegistroDTO();
        dto.setCategoriaId(1);
        dto.setSubcategoriaId(1);
        dto.setZona(1);
        dto.setDireccionExacta("Calle Principal, San Juan Sacatepéquez");
        dto.setPuntoReferencia("Frente al parque");
        dto.setLatitud(new BigDecimal("14.7175000"));
        dto.setLongitud(new BigDecimal("-90.6442000"));
        dto.setDescripcion("La luminaria pública se encuentra apagada desde hace varios días.");

        MultipartFile foto = new MockMultipartFile(
                "fotos",
                "evidencia.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
        );

        when(usuarioRepository.findByCorreoIgnoreCase("ciudadano@test.com")).thenReturn(Optional.of(ciudadano));
        when(catalogoService.obtenerCategoria(1)).thenReturn(categoria);
        when(catalogoService.obtenerSubcategoria(1)).thenReturn(subcategoria);
        when(asignacionAutomaticaService.asignarFuncionarioAutomatico()).thenReturn(Optional.empty());
        when(fileStorageService.guardarArchivo(foto)).thenReturn("/subidas/evidencias/foto.jpg");
        when(quejaRepository.save(any(Queja.class))).thenAnswer(invocation -> {
            Queja queja = invocation.getArgument(0);
            if (queja.getQuejaId() == null) {
                queja.setQuejaId(99);
            }
            return queja;
        });

        quejaService.registrarQueja(dto, List.of(foto), "ciudadano@test.com");

        ArgumentCaptor<Queja> captor = ArgumentCaptor.forClass(Queja.class);
        verify(quejaRepository, atLeastOnce()).save(captor.capture());

        Queja guardada = captor.getAllValues().get(0);
        assertSame(categoria, guardada.getCategoria());
        assertSame(subcategoria, guardada.getSubcategoria());
        assertEquals("QUE-" + java.time.Year.now().getValue() + "-000099", guardada.getCorrelativo());
    }

    @Test
    void registrarQuejaDerivadaDebeVincularExpedienteOrigenYConservarCategoria() {
        CategoriaQueja categoria = new CategoriaQueja();
        categoria.setCategoriaId(1);
        categoria.setNombreCategoria("Alumbrado Público");

        SubcategoriaQueja subcategoria = new SubcategoriaQueja();
        subcategoria.setSubcategoriaId(1);
        subcategoria.setCategoria(categoria);
        subcategoria.setNombreSubcategoria("Luminaria apagada / quemada");

        Credencial credencial = new Credencial();
        credencial.setUsername("ciudadano@test.com");

        Usuario ciudadano = new Usuario();
        ciudadano.setUsuarioId(3);
        ciudadano.setCorreo("ciudadano@test.com");
        ciudadano.setCredencial(credencial);

        Queja padre = new Queja();
        padre.setQuejaId(50);
        padre.setCorrelativo("QUE-2026-000050");
        padre.setCiudadano(ciudadano);
        padre.setCategoria(categoria);
        padre.setSubcategoria(subcategoria);
        padre.setEstadoActual("EN INSPECCIÓN");
        padre.setZona(1);
        padre.setDireccionExacta("Calle Principal, San Juan Sacatepéquez");
        padre.setLatitud(new BigDecimal("14.7175000"));
        padre.setLongitud(new BigDecimal("-90.6442000"));

        com.muni.backend.quejas.dto.ciudadano.QuejaDerivadaDTO dto =
                new com.muni.backend.quejas.dto.ciudadano.QuejaDerivadaDTO();
        dto.setCorrelativoPadre("QUE-2026-000050");
        dto.setTipoDerivacion("AGRAVAMIENTO");
        dto.setDescripcion("El problema continúa y ha empeorado durante los últimos días.");

        MultipartFile foto = new MockMultipartFile(
                "fotos",
                "evidencia.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
        );

        when(quejaRepository.findByCorrelativo("QUE-2026-000050")).thenReturn(Optional.of(padre));
        when(usuarioRepository.findByCorreoIgnoreCase("ciudadano@test.com")).thenReturn(Optional.of(ciudadano));
        when(asignacionAutomaticaService.asignarFuncionarioAutomatico()).thenReturn(Optional.empty());
        when(fileStorageService.guardarArchivo(foto)).thenReturn("/subidas/evidencias/foto.jpg");
        when(quejaRepository.save(any(Queja.class))).thenAnswer(invocation -> {
            Queja queja = invocation.getArgument(0);
            if (queja.getQuejaId() == null) {
                queja.setQuejaId(100);
            }
            return queja;
        });

        quejaService.registrarQuejaDerivada(dto, List.of(foto), "ciudadano@test.com");

        ArgumentCaptor<Queja> captor = ArgumentCaptor.forClass(Queja.class);
        verify(quejaRepository, atLeastOnce()).save(captor.capture());

        Queja guardada = captor.getAllValues().get(0);
        assertSame(padre, guardada.getQuejaOrigen());
        assertEquals("AGRAVAMIENTO", guardada.getTipoRegistro());
        assertSame(categoria, guardada.getCategoria());
        assertSame(subcategoria, guardada.getSubcategoria());
    }
}
