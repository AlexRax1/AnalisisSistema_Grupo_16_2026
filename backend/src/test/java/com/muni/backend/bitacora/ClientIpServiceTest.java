package com.muni.backend.bitacora;

import com.muni.backend.bitacora.service.ClientIpService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClientIpServiceTest {

    private final ClientIpService clientIpService = new ClientIpService();

    @AfterEach
    void limpiarContexto() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void debeTomarLaPrimeraIpValidaDeXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown, 203.0.113.10, 10.0.0.5");
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("203.0.113.10", clientIpService.obtenerIp());
    }

    @Test
    void debeUsarXRealIpCuandoNoHayXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "198.51.100.20");
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("198.51.100.20", clientIpService.obtenerIp());
    }

    @Test
    void debePriorizarXRealIpSobreXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "198.51.100.20");
        request.addHeader("X-Forwarded-For", "203.0.113.10");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("198.51.100.20", clientIpService.obtenerIp());
    }

    @Test
    void debeUsarRemoteAddrComoUltimoRecurso() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.30");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("192.0.2.30", clientIpService.obtenerIp());
    }

    @Test
    void debeQuitarPuertoDeIpv6EntreCorchetes() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "[2001:db8::1]:443");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("2001:db8::1", clientIpService.obtenerIp());
    }

    @Test
    void debeRetornarNullFueraDeUnaPeticionHttp() {
        assertNull(clientIpService.obtenerIp());
    }
}
