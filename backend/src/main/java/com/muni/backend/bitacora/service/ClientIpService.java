package com.muni.backend.bitacora.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Obtiene la IP del cliente sin acoplar la bitácora a un controller.
@Component
public class ClientIpService {

    private static final int MAX_IP_LENGTH = 45;

    public String obtenerIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }

        HttpServletRequest request = servletAttributes.getRequest();
        String ip = normalizar(request.getHeader("X-Real-IP"));

        if (ip == null) {
            ip = primeraIpValida(request.getHeader("X-Forwarded-For"));
        }
        if (ip == null) {
            ip = normalizar(request.getRemoteAddr());
        }
        return ip;
    }

    private String primeraIpValida(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        for (String parte : header.split(",")) {
            String ip = normalizar(parte);
            if (ip != null) {
                return ip;
            }
        }
        return null;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }

        String ip = valor.trim();
        if (ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return null;
        }

        // Algunos proxies envían IPv6 entre corchetes y con puerto: [::1]:1234.
        if (ip.startsWith("[")) {
            int cierre = ip.indexOf(']');
            if (cierre > 1) {
                ip = ip.substring(1, cierre);
            }
        } else {
            // IPv4 con puerto: 203.0.113.10:1234.
            int dosPuntos = ip.indexOf(':');
            int ultimoDosPuntos = ip.lastIndexOf(':');
            if (dosPuntos == ultimoDosPuntos && dosPuntos > 0) {
                String posiblePuerto = ip.substring(dosPuntos + 1);
                if (posiblePuerto.chars().allMatch(Character::isDigit)) {
                    ip = ip.substring(0, dosPuntos);
                }
            }
        }

        return ip.length() <= MAX_IP_LENGTH ? ip : null;
    }
}
