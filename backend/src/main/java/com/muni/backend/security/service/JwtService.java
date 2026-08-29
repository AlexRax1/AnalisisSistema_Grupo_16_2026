package com.muni.backend.security.service;

import com.muni.backend.security.model.Credencial;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- CREACIÓN ---

    public String generarToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put("roles", roles);

        if (userDetails instanceof Credencial) {
            Credencial credencial = (Credencial) userDetails;
            claims.put("userId", credencial.getUserId());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 minutos
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // --- VALIDACIÓN Y EXTRACCIÓN ---

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public Integer getUserIdFromToken(String token) {
        return getAllClaimsFromToken(token).get("userId", Integer.class);
    }

    public List<String> getRolesFromToken(String token) {
        String rolesStr = getAllClaimsFromToken(token).get("roles", String.class);
        if (rolesStr == null || rolesStr.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(rolesStr.split(","));
    }
}