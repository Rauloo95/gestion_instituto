package com.baleras.api.Controller;

import com.baleras.api.Dto.CentroRequest;
import com.baleras.api.Dto.CentroResponse;
import com.baleras.api.Exception.GlobalExceptionHandler;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.UsuarioRepository;
import com.baleras.api.Security.JwtAuthenticationFilter;
import com.baleras.api.Service.CentroService;
import com.baleras.api.Service.JwtService;

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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CentroController.class)
@Import({
        TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
class CentroControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private CentroService centroService;

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

    private CentroResponse crearCentroResponse() {
        return new CentroResponse(
                1L,
                "Centro Baleras",
                "Educativo",
                "Calle Mayor 10",
                "Madrid",
                "28001",
                "600123456",
                "centro@email.com",
                true
        );
    }

    private String jsonCentro() {
        return """
                {
                    "nombre": "Centro Baleras",
                    "tipo": "Educativo",
                    "direccion": "Calle Mayor 10",
                    "ciudad": "Madrid",
                    "codigoPostal": "28001",
                    "telefono": "600123456",
                    "email": "centro@email.com"
                }
                """;
    }

    // -----------------------------------------
    // GET /api/centros
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerTodos_admin_devuelve200() throws Exception {

        when(centroService.obtenerTodos())
                .thenReturn(List.of(crearCentroResponse()));

        mockMvc.perform(get("/api/centros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Centro Baleras"))
                .andExpect(jsonPath("$[0].tipo").value("Educativo"));

        verify(centroService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerTodos_profesor_devuelve200() throws Exception {

        when(centroService.obtenerTodos())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/centros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(centroService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerTodos_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/centros"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(centroService);
    }

    // -----------------------------------------
    // GET /api/centros/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_admin_devuelve200() throws Exception {

        when(centroService.obtenerPorId(1L))
                .thenReturn(crearCentroResponse());

        mockMvc.perform(get("/api/centros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Centro Baleras"));

        verify(centroService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorId_profesor_devuelve200() throws Exception {

        when(centroService.obtenerPorId(1L))
                .thenReturn(crearCentroResponse());

        mockMvc.perform(get("/api/centros/1"))
                .andExpect(status().isOk());

        verify(centroService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorId_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/centros/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(centroService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_inexistente_devuelve404() throws Exception {

        when(centroService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Centro no encontrado"));

        mockMvc.perform(get("/api/centros/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(centroService).obtenerPorId(99L);
    }

    // -----------------------------------------
    // POST /api/centros
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_admin_devuelve201() throws Exception {

        when(centroService.crear(any(CentroRequest.class)))
                .thenReturn(crearCentroResponse());

        mockMvc.perform(post("/api/centros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCentro()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Centro Baleras"));

        verify(centroService).crear(any(CentroRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void crear_profesor_devuelve403() throws Exception {

        mockMvc.perform(post("/api/centros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCentro()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(centroService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void crear_alumno_devuelve403() throws Exception {

        mockMvc.perform(post("/api/centros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCentro()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(centroService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_nombreVacio_devuelve400() throws Exception {

        String json = """
                {
                    "nombre": "",
                    "tipo": "Educativo",
                    "direccion": "Calle Mayor 10",
                    "ciudad": "Madrid",
                    "codigoPostal": "28001",
                    "telefono": "600123456",
                    "email": "centro@email.com"
                }
                """;

        mockMvc.perform(post("/api/centros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.nombre").exists());

        verifyNoInteractions(centroService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_tipoVacio_devuelve400() throws Exception {

        String json = """
                {
                    "nombre": "Centro Baleras",
                    "tipo": "",
                    "direccion": "Calle Mayor 10",
                    "ciudad": "Madrid",
                    "codigoPostal": "28001",
                    "telefono": "600123456",
                    "email": "centro@email.com"
                }
                """;

        mockMvc.perform(post("/api/centros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.tipo").exists());

        verifyNoInteractions(centroService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_emailInvalido_devuelve400() throws Exception {

        String json = """
                {
                    "nombre": "Centro Baleras",
                    "tipo": "Educativo",
                    "direccion": "Calle Mayor 10",
                    "ciudad": "Madrid",
                    "codigoPostal": "28001",
                    "telefono": "600123456",
                    "email": "email-invalido"
                }
                """;

        mockMvc.perform(post("/api/centros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists());

        verifyNoInteractions(centroService);
    }

    // -----------------------------------------
    // PUT /api/centros/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_admin_devuelve200() throws Exception {

        when(centroService.actualizar(eq(1L), any(CentroRequest.class)))
                .thenReturn(crearCentroResponse());

        mockMvc.perform(put("/api/centros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCentro()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(centroService).actualizar(eq(1L), any(CentroRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void actualizar_profesor_devuelve403() throws Exception {

        mockMvc.perform(put("/api/centros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCentro()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(centroService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void actualizar_alumno_devuelve403() throws Exception {

        mockMvc.perform(put("/api/centros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCentro()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(centroService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_inexistente_devuelve404() throws Exception {

        when(centroService.actualizar(eq(99L), any(CentroRequest.class)))
                .thenThrow(new ResourceNotFoundException("Centro no encontrado"));

        mockMvc.perform(put("/api/centros/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCentro()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(centroService).actualizar(eq(99L), any(CentroRequest.class));
    }

    // -----------------------------------------
    // DELETE /api/centros/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_admin_devuelve204() throws Exception {

        doNothing().when(centroService).eliminar(1L);

        mockMvc.perform(delete("/api/centros/1"))
                .andExpect(status().isNoContent());

        verify(centroService).eliminar(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void eliminar_profesor_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/centros/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(centroService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void eliminar_alumno_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/centros/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(centroService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_inexistente_devuelve404() throws Exception {

        doThrow(new ResourceNotFoundException("Centro no encontrado"))
                .when(centroService).eliminar(99L);

        mockMvc.perform(delete("/api/centros/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(centroService).eliminar(99L);
    }

    // -----------------------------------------
    // AUTENTICACIÓN
    // -----------------------------------------

    @Test
    void obtenerTodos_sinAutenticacion_devuelve401() throws Exception {

        mockMvc.perform(get("/api/centros"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(centroService);
    }
}
