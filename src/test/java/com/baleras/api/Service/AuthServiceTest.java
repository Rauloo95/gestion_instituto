package com.baleras.api.Service;

import com.baleras.api.Dto.AuthRequest;
import com.baleras.api.Dto.AuthResponse;
import com.baleras.api.Entity.Rol;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Exception.InvalidCredentialException;
import com.baleras.api.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Raúl")
                .apellidos("Gómez")
                .email("raul@email.com")
                .password("hash")
                .rol(Rol.ALUMNO)
                .build();
    }

    @Test
    void login_credencialesCorrectas_deberiaDevolverToken() {

        AuthRequest request = new AuthRequest(
                "raul@email.com",
                "123456"
        );

        when(usuarioRepository.findByEmail("raul@email.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("123456", "hash"))
                .thenReturn(true);

        when(jwtService.generarToken(usuario))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("raul@email.com", response.email());
        assertEquals(Rol.ALUMNO, response.rol());
        assertEquals("jwt-token", response.token());

        verify(usuarioRepository).findByEmail("raul@email.com");
        verify(passwordEncoder).matches("123456", "hash");
        verify(jwtService).generarToken(usuario);
    }

    @Test
    void login_emailInexistente_deberiaLanzarExcepcion() {

        AuthRequest request = new AuthRequest(
                "noexiste@email.com",
                "123456"
        );

        when(usuarioRepository.findByEmail("noexiste@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialException.class,
                () -> authService.login(request)
        );

        verify(usuarioRepository).findByEmail("noexiste@email.com");

        verify(jwtService, never())
                .generarToken(any());
    }

    @Test
    void login_passwordIncorrecta_deberiaLanzarExcepcion() {

        AuthRequest request = new AuthRequest(
                "raul@email.com",
                "incorrecta"
        );

        when(usuarioRepository.findByEmail("raul@email.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("incorrecta", "hash"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialException.class,
                () -> authService.login(request)
        );

        verify(usuarioRepository).findByEmail("raul@email.com");

        verify(passwordEncoder)
                .matches("incorrecta", "hash");

        verify(jwtService, never())
                .generarToken(any());
    }
}
