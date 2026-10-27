package com.baleras.api.Service;

import com.baleras.api.Dto.AuthRequest;
import com.baleras.api.Dto.AuthResponse;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Exception.InvalidCredentialException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(AuthRequest request) {

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new InvalidCredentialException(
                                "Email o contraseña incorrectos"
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                usuario.getPassword())) {

            throw new InvalidCredentialException(
                    "Email o contraseña incorrectos"
            );
        }
        String token = jwtService.generarToken(usuario);
        return new AuthResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                token

        );
    }
}
