package com.baleras.api.Service;

import com.baleras.api.Entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    // Inyección directa del secret con fallback
    public JwtService(@Value("${jwt.secret:${JWT_SECRET:ClaveSecretaSuperLargaYCompletamenteSeguraParaFirmarTokensJWT2026Baleras}}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("La clave JWT no puede ser nula ni vacía");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusSeconds(3600);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("role", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(secretKey)
                .compact();
    }

    public String obtenerEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean esValido(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}