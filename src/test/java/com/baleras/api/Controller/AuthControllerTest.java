package com.baleras.api.Controller;

import com.baleras.api.Dto.AuthRequest;
import com.baleras.api.Dto.AuthResponse;
import com.baleras.api.Dto.UsuarioRequest;
import com.baleras.api.Dto.UsuarioResponse;
import com.baleras.api.Entity.Rol;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.GlobalExceptionHandler;
import com.baleras.api.Exception.InvalidCredentialException;
import com.baleras.api.Repository.UsuarioRepository;
import com.baleras.api.Security.JwtAuthenticationFilter;
import com.baleras.api.Service.AuthService;
import com.baleras.api.Service.JwtService;
import com.baleras.api.Service.UsuarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({
        TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        doAnswer(invocation -> {
            ServletRequest req = invocation.getArgument(0);
            ServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    // =========================================================================
    // POST /api/auth/login
    // =========================================================================
    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("200 OK - Login exitoso devuelve AuthResponse con token")
        void loginExitoso() throws Exception {
            AuthResponse response = new AuthResponse(
                    1L,
                    "Juan",
                    "juan@test.com",
                    Rol.ALUMNO,
                    "jwt-token-123"
            );

            when(authService.login(any(AuthRequest.class))).thenReturn(response);

            String jsonRequest = """
                    {
                        "email": "juan@test.com",
                        "password": "password123"
                    }
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.email").value("juan@test.com"))
                    .andExpect(jsonPath("$.rol").value("ALUMNO"))
                    .andExpect(jsonPath("$.token").value("jwt-token-123"));

            verify(authService).login(any(AuthRequest.class));
        }

        @Test
        @DisplayName("401 Unauthorized - Credenciales incorrectas lanza InvalidCredentialException")
        void loginCredencialesIncorrectas() throws Exception {
            when(authService.login(any(AuthRequest.class)))
                    .thenThrow(new InvalidCredentialException("Email o contraseña incorrectos"));

            String jsonRequest = """
                    {
                        "email": "juan@test.com",
                        "password": "wrongpassword"
                    }
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Email o contraseña incorrectos"));
        }

        @Test
        @DisplayName("400 Bad Request - Email vacío")
        void loginEmailVacio() throws Exception {
            String jsonRequest = """
                    {
                        "email": "",
                        "password": "password123"
                    }
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.errors.email").value("El email es obligatorio"));

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("400 Bad Request - Email con formato inválido")
        void loginEmailInvalido() throws Exception {
            String jsonRequest = """
                    {
                        "email": "no-es-un-email",
                        "password": "password123"
                    }
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.errors.email").value("El email no tiene un formato válido"));

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("400 Bad Request - Contraseña vacía")
        void loginPasswordVacio() throws Exception {
            String jsonRequest = """
                    {
                        "email": "juan@test.com",
                        "password": ""
                    }
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.errors.password").value("La contraseña es obligatoria"));

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("400 Bad Request - Email y contraseña nulos")
        void loginCamposNulos() throws Exception {
            String jsonRequest = """
                    {}
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.errors.email").value("El email es obligatorio"))
                    .andExpect(jsonPath("$.errors.password").value("La contraseña es obligatoria"));

            verifyNoInteractions(authService);
        }
    }

    // =========================================================================
    // POST /api/auth/register
    // =========================================================================
    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("201 Created - Registro exitoso asignando rol ALUMNO")
        void registroExitoso() throws Exception {
            UsuarioResponse response = new UsuarioResponse(
                    1L,
                    "Pedro",
                    "Gómez Gómez",
                    "pedro@test.com",
                    Rol.ALUMNO
            );

            when(usuarioService.crear(any(UsuarioRequest.class))).thenReturn(response);

            // Incluso si se especifica otro rol en el body, AuthController debe forzar Rol.ALUMNO
            String jsonRequest = """
                    {
                        "nombre": "Pedro",
                        "apellidos": "Gómez Gómez",
                        "email": "pedro@test.com",
                        "password": "password123",
                        "rol": "ALUMNO"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nombre").value("Pedro"))
                    .andExpect(jsonPath("$.apellidos").value("Gómez Gómez"))
                    .andExpect(jsonPath("$.email").value("pedro@test.com"))
                    .andExpect(jsonPath("$.rol").value("ALUMNO"));

            ArgumentCaptor<UsuarioRequest> captor = ArgumentCaptor.forClass(UsuarioRequest.class);
            verify(usuarioService).crear(captor.capture());
            UsuarioRequest capturedRequest = captor.getValue();
            assertThat(capturedRequest.nombre()).isEqualTo("Pedro");
            assertThat(capturedRequest.apellidos()).isEqualTo("Gómez Gómez");
            assertThat(capturedRequest.email()).isEqualTo("pedro@test.com");
            assertThat(capturedRequest.password()).isEqualTo("password123");
            assertThat(capturedRequest.rol()).isEqualTo(Rol.ALUMNO);
        }

        @Test
        @DisplayName("409 Conflict - Email ya registrado lanza DuplicateResourceException")
        void registroEmailDuplicado() throws Exception {
            when(usuarioService.crear(any(UsuarioRequest.class)))
                    .thenThrow(new DuplicateResourceException("Ya existe un usuario con el email: pedro@test.com"));

            String jsonRequest = """
                    {
                        "nombre": "Pedro",
                        "apellidos": "Gómez Gómez",
                        "email": "pedro@test.com",
                        "password": "password123",
                        "rol": "ALUMNO"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("Ya existe un usuario con el email: pedro@test.com"));
        }

        @Test
        @DisplayName("400 Bad Request - Nombre vacío")
        void registroNombreVacio() throws Exception {
            String jsonRequest = """
                    {
                        "nombre": "",
                        "apellidos": "Gómez Gómez",
                        "email": "pedro@test.com",
                        "password": "password123",
                        "rol": "ALUMNO"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.errors.nombre").value("El nombre es obligatorio"));

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("400 Bad Request - Nombre supera 50 caracteres")
        void registroNombreLargo() throws Exception {
            String nombreLargo = "a".repeat(51);
            String jsonRequest = String.format("""
                    {
                        "nombre": "%s",
                        "apellidos": "Gómez Gómez",
                        "email": "pedro@test.com",
                        "password": "password123",
                        "rol": "ALUMNO"
                    }
                    """, nombreLargo);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.nombre").value("El nombre no puede superar los 50 caracteres"));

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("400 Bad Request - Apellidos vacíos")
        void registroApellidosVacios() throws Exception {
            String jsonRequest = """
                    {
                        "nombre": "Pedro",
                        "apellidos": "",
                        "email": "pedro@test.com",
                        "password": "password123",
                        "rol": "ALUMNO"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.apellidos").value("Los apellidos son obligatorios"));

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("400 Bad Request - Apellidos superan 100 caracteres")
        void registroApellidosLargos() throws Exception {
            String apellidosLargos = "b".repeat(101);
            String jsonRequest = String.format("""
                    {
                        "nombre": "Pedro",
                        "apellidos": "%s",
                        "email": "pedro@test.com",
                        "password": "password123",
                        "rol": "ALUMNO"
                    }
                    """, apellidosLargos);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.apellidos").value("Los apellidos no pueden superar los 100 caracteres"));

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("400 Bad Request - Email vacío")
        void registroEmailVacio() throws Exception {
            String jsonRequest = """
                    {
                        "nombre": "Pedro",
                        "apellidos": "Gómez",
                        "email": "",
                        "password": "password123",
                        "rol": "ALUMNO"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").value("El email es obligatorio"));

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("400 Bad Request - Email con formato inválido")
        void registroEmailInvalido() throws Exception {
            String jsonRequest = """
                    {
                        "nombre": "Pedro",
                        "apellidos": "Gómez",
                        "email": "email-invalido",
                        "password": "password123",
                        "rol": "ALUMNO"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").value("El email no tiene un formato válido"));

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("400 Bad Request - Contraseña vacía")
        void registroPasswordVacio() throws Exception {
            String jsonRequest = """
                    {
                        "nombre": "Pedro",
                        "apellidos": "Gómez",
                        "email": "pedro@test.com",
                        "password": "",
                        "rol": "ALUMNO"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").exists());

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("400 Bad Request - Contraseña menor a 6 caracteres")
        void registroPasswordCorto() throws Exception {
            String jsonRequest = """
                    {
                        "nombre": "Pedro",
                        "apellidos": "Gómez",
                        "email": "pedro@test.com",
                        "password": "12345",
                        "rol": "ALUMNO"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").value("La contraseña debe tener al menos 6 caracteres"));

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("400 Bad Request - Rol nulo en la petición")
        void registroRolNulo() throws Exception {
            String jsonRequest = """
                    {
                        "nombre": "Pedro",
                        "apellidos": "Gómez",
                        "email": "pedro@test.com",
                        "password": "password123",
                        "rol": null
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.rol").value("El rol es obligatorio"));

            verifyNoInteractions(usuarioService);
        }
    }
}
