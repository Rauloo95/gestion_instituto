package com.baleras.api.Controller;

import com.baleras.api.Dto.UsuarioRequest;
import com.baleras.api.Dto.UsuarioResponse;
import com.baleras.api.Entity.Rol;
import com.baleras.api.Exception.GlobalExceptionHandler;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.UsuarioRepository;
import com.baleras.api.Security.JwtAuthenticationFilter;
import com.baleras.api.Service.JwtService;
import com.baleras.api.Service.UsuarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import({
                TestSecurityConfig.class,
                GlobalExceptionHandler.class
})
class UsuarioControllerTest {

        @Autowired
        private WebApplicationContext context;

        private MockMvc mockMvc;

        @MockitoBean
        private UsuarioService usuarioService;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UsuarioRepository usuarioRepository;

        @BeforeEach
        void setup() throws Exception {
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

        private UsuarioResponse crearUsuarioResponse() {
                return new UsuarioResponse(
                                1L,
                                "Raúl",
                                "Gómez",
                                "raul@test.com",
                                Rol.ADMIN);
        }

        private String jsonUsuario() {
                return """
                                {
                                    "nombre": "Raúl",
                                    "apellidos": "Gómez",
                                    "email": "raul@test.com",
                                    "password": "123456",
                                    "rol": "ADMIN"
                                }
                                """;
        }

        // -----------------------------------------
        // GET /api/usuarios
        // -----------------------------------------

        @Test
        @WithMockUser(roles = "ADMIN")
        void obtenerTodos_admin_devuelve200() throws Exception {

                when(usuarioService.obtenerTodos())
                                .thenReturn(java.util.List.of(crearUsuarioResponse()));

                mockMvc.perform(get("/api/usuarios"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].nombre").value("Raúl"))
                                .andExpect(jsonPath("$[0].email").value("raul@test.com"));

                verify(usuarioService).obtenerTodos();
        }

        @Test
        @WithMockUser(roles = "PROFESOR")
        void obtenerTodos_profesor_devuelve200() throws Exception {

                when(usuarioService.obtenerTodos())
                                .thenReturn(java.util.List.of());

                mockMvc.perform(get("/api/usuarios"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @WithMockUser(roles = "ALUMNO")
        void obtenerTodos_alumno_devuelve403() throws Exception {

                mockMvc.perform(get("/api/usuarios"))
                                .andExpect(status().isForbidden());

                verifyNoInteractions(usuarioService);
        }

        // -----------------------------------------
        // GET /api/usuarios/{id}
        // -----------------------------------------

        @Test
        @WithMockUser(roles = "ADMIN")
        void obtenerPorId_admin_devuelve200() throws Exception {

                when(usuarioService.obtenerPorId(1L))
                                .thenReturn(crearUsuarioResponse());

                mockMvc.perform(get("/api/usuarios/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.nombre").value("Raúl"));
        }

        @Test
        @WithMockUser(roles = "PROFESOR")
        void obtenerPorId_profesor_devuelve200() throws Exception {

                when(usuarioService.obtenerPorId(1L))
                                .thenReturn(crearUsuarioResponse());

                mockMvc.perform(get("/api/usuarios/1"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ALUMNO")
        void obtenerPorId_alumno_devuelve403() throws Exception {

                mockMvc.perform(get("/api/usuarios/1"))
                                .andExpect(status().isForbidden());

                verifyNoInteractions(usuarioService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void obtenerPorId_inexistente_devuelve404() throws Exception {

                when(usuarioService.obtenerPorId(99L))
                                .thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

                mockMvc.perform(get("/api/usuarios/99"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        // -----------------------------------------
        // POST /api/usuarios
        // -----------------------------------------

        @Test
        @WithMockUser(roles = "ADMIN")
        void crear_admin_devuelve201() throws Exception {

                when(usuarioService.crear(any(UsuarioRequest.class)))
                                .thenReturn(crearUsuarioResponse());

                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonUsuario()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.email").value("raul@test.com"));

                verify(usuarioService).crear(any(UsuarioRequest.class));
        }

        @Test
        @WithMockUser(roles = "PROFESOR")
        void crear_profesor_devuelve403() throws Exception {

                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonUsuario()))
                                .andExpect(status().isForbidden());

                verifyNoInteractions(usuarioService);
        }

        @Test
        @WithMockUser(roles = "ALUMNO")
        void crear_alumno_devuelve403() throws Exception {

                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonUsuario()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void crear_nombreVacio_devuelve400() throws Exception {

                String json = """
                                {
                                    "nombre": "",
                                    "apellidos": "Gómez",
                                    "email": "raul@test.com",
                                    "password": "123456",
                                    "rol": "ADMIN"
                                }
                                """;

                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.errors.nombre").exists());

                verifyNoInteractions(usuarioService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void crear_emailInvalido_devuelve400() throws Exception {

                String json = """
                                {
                                    "nombre": "Raúl",
                                    "apellidos": "Gómez",
                                    "email": "correo-invalido",
                                    "password": "123456",
                                    "rol": "ADMIN"
                                }
                                """;

                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors.email").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void crear_passwordCorta_devuelve400() throws Exception {

                String json = """
                                {
                                    "nombre": "Raúl",
                                    "apellidos": "Gómez",
                                    "email": "raul@test.com",
                                    "password": "123",
                                    "rol": "ADMIN"
                                }
                                """;

                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors.password").exists());
        }

        // -----------------------------------------
        // PUT /api/usuarios/{id}
        // -----------------------------------------

        @Test
        @WithMockUser(roles = "ADMIN")
        void actualizar_admin_devuelve200() throws Exception {

                when(usuarioService.actualizar(eq(1L), any(UsuarioRequest.class)))
                                .thenReturn(crearUsuarioResponse());

                mockMvc.perform(put("/api/usuarios/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonUsuario()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1));

                verify(usuarioService).actualizar(eq(1L), any(UsuarioRequest.class));
        }

        @Test
        @WithMockUser(roles = "PROFESOR")
        void actualizar_profesor_devuelve403() throws Exception {

                mockMvc.perform(put("/api/usuarios/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonUsuario()))
                                .andExpect(status().isForbidden());

                verifyNoInteractions(usuarioService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void actualizar_inexistente_devuelve404() throws Exception {

                when(usuarioService.actualizar(eq(99L), any(UsuarioRequest.class)))
                                .thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

                mockMvc.perform(put("/api/usuarios/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonUsuario()))
                                .andExpect(status().isNotFound());
        }

        // -----------------------------------------
        // DELETE /api/usuarios/{id}
        // -----------------------------------------

        @Test
        @WithMockUser(roles = "ADMIN")
        void eliminar_admin_devuelve204() throws Exception {

                doNothing().when(usuarioService).eliminar(1L);

                mockMvc.perform(delete("/api/usuarios/1"))
                                .andExpect(status().isNoContent());

                verify(usuarioService).eliminar(1L);
        }

        @Test
        @WithMockUser(roles = "PROFESOR")
        void eliminar_profesor_devuelve403() throws Exception {

                mockMvc.perform(delete("/api/usuarios/1"))
                                .andExpect(status().isForbidden());

                verifyNoInteractions(usuarioService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void eliminar_inexistente_devuelve404() throws Exception {

                doThrow(new ResourceNotFoundException("Usuario no encontrado"))
                                .when(usuarioService).eliminar(99L);

                mockMvc.perform(delete("/api/usuarios/99"))
                                .andExpect(status().isNotFound());
        }

        // -----------------------------------------
        // AUTENTICACIÓN
        // -----------------------------------------

        @Test
        void obtenerTodos_sinAutenticacion_devuelve401() throws Exception {

                mockMvc.perform(get("/api/usuarios"))
                                .andExpect(status().isUnauthorized());

                verifyNoInteractions(usuarioService);
        }
}
