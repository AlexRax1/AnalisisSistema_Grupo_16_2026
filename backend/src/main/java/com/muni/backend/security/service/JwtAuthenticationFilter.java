package com.muni.backend.security.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

        // 1. Ver qué header está llegando
        System.out.println(">>> REQUEST URI: " + request.getRequestURI());
        System.out.println(">>> AUTH HEADER RECIBIDO: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println(">>> No hay Bearer token, dejando pasar sin autenticación");
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(token)) {
                String username = jwtService.getUsernameFromToken(token);
                List<String> roles = jwtService.getRolesFromToken(token);

                System.out.println(">>> TOKEN VÁLIDO. Usuario: " + username + " | Roles: " + roles);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = obtenerAutoridadesConPrefijo(roles);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } else {
                System.out.println(">>> EL TOKEN NO ES VÁLIDO SEGÚN jwtService.isTokenValid()");
            }
        } catch (Exception e) {
            System.err.println(">>> EXCEPCIÓN EN FILTRO JWT: " + e.getMessage());
            e.printStackTrace(); // Muestra el stacktrace exacto en la consola de Spring Boot
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