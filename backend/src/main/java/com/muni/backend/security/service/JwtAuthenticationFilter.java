package com.muni.backend.security.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Ignora peticiones preflight (OPTIONS)
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/subidas/")
                || path.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Sin token Bearer para {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(token)) {
                String username = jwtService.getUsernameFromToken(token);
                List<String> roles = jwtService.getRolesFromToken(token);

                log.debug("JWT válido para usuario {} con roles {}", username, roles);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = obtenerAutoridadesConPrefijo(roles);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } else {
                log.debug("Token JWT inválido para {}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.warn("Error validando JWT para {}: {}", request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> obtenerAutoridadesConPrefijo(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (String role : roles) {
            if (role == null || role.isBlank()) continue;

            String roleLimpio = role.trim();
            // 1. Añadimos el rol exacto como viene en la base de datos (ej. "ADMINISTRADOR")
            authorities.add(new SimpleGrantedAuthority(roleLimpio));

            // 2. Si no tiene el prefijo "ROLE_", añadimos también la versión con prefijo (ej. "ROLE_ADMINISTRADOR")
            if (!roleLimpio.startsWith("ROLE_")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleLimpio));
            }
        }

        return authorities;
    }
}