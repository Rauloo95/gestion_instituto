package com.baleras.api.Service;

import com.baleras.api.Dto.UsuarioRequest;
import com.baleras.api.Dto.UsuarioResponse;
import com.baleras.api.Entity.Rol;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Raúl")
                .apellidos("Gómez")
                .email("raul@email.com")
                .password("password-encriptada")
                .rol(Rol.ALUMNO)
                .build();
    }

    @Test
    void obtenerPorId_deberiaDevolverUsuario() {

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.obtenerPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Raúl", response.nombre());
        assertEquals("raul@email.com", response.email());
        assertEquals(Rol.ALUMNO, response.rol());

        verify(usuarioRepository).findById(1L);
    }

    @Test
    void obtenerPorId_usuarioNoExiste_deberiaLanzarExcepcion() {

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> usuarioService.obtenerPorId(99L)
        );

        verify(usuarioRepository).findById(99L);
    }

    @Test
    void crear_emailDuplicado_deberiaLanzarExcepcion() {

        UsuarioRequest request = new UsuarioRequest(
                "Raúl",
                "Gómez",
                "raul@email.com",
                "123456",
                Rol.ALUMNO
        );

        when(usuarioRepository.existsByEmail("raul@email.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> usuarioService.crear(request)
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crear_usuarioValido_deberiaGuardarUsuario() {

        UsuarioRequest request = new UsuarioRequest(
                "Raúl",
                "Gómez",
                "raul@email.com",
                "123456",
                Rol.ALUMNO
        );

        when(usuarioRepository.existsByEmail("raul@email.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("123456"))
                .thenReturn("password-encriptada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario);

        UsuarioResponse response = usuarioService.crear(request);

        assertNotNull(response);
        assertEquals("raul@email.com", response.email());

        verify(passwordEncoder).encode("123456");
        verify(usuarioRepository).save(any(Usuario.class));
    }
}
