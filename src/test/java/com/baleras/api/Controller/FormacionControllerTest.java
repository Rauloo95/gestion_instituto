package com.baleras.api.Controller;

import com.baleras.api.Dto.FormacionRequest;
import com.baleras.api.Dto.FormacionResponse;
import com.baleras.api.Exception.GlobalExceptionHandler;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.UsuarioRepository;
import com.baleras.api.Security.JwtAuthenticationFilter;
import com.baleras.api.Service.FormacionService;
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

@WebMvcTest(FormacionController.class)
@Import({
        TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
class FormacionControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private FormacionService formacionService;

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

    private FormacionResponse crearFormacionResponse() {
        return new FormacionResponse(
                1L,
                "Desarrollo de Aplicaciones Web",
                "Ciclo Formativo de Grado Superior",
                "Grado Superior",
                "DAW",
                1L,
                "Centro Baleras"
        );
    }

    private String jsonFormacion() {
        return """
                {
                    "nombre": "Desarrollo de Aplicaciones Web",
                    "descripcion": "Ciclo Formativo de Grado Superior",
                    "nivel": "Grado Superior",
                    "codigo": "DAW",
                    "centroId": 1
                }
                """;
    }

    // -----------------------------------------
    // GET /api/formaciones
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerTodos_admin_devuelve200() throws Exception {

        when(formacionService.obtenerTodos())
                .thenReturn(List.of(crearFormacionResponse()));

        mockMvc.perform(get("/api/formaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Desarrollo de Aplicaciones Web"))
                .andExpect(jsonPath("$[0].codigo").value("DAW"));

        verify(formacionService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerTodos_profesor_devuelve200() throws Exception {

        when(formacionService.obtenerTodos())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/formaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(formacionService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerTodos_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/formaciones"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(formacionService);
    }

    // -----------------------------------------
    // GET /api/formaciones/centro/{centroId}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorCentro_admin_devuelve200() throws Exception {

        when(formacionService.obtenerPorCentro(1L))
                .thenReturn(List.of(crearFormacionResponse()));

        mockMvc.perform(get("/api/formaciones/centro/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].centroId").value(1));

        verify(formacionService).obtenerPorCentro(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorCentro_profesor_devuelve200() throws Exception {

        when(formacionService.obtenerPorCentro(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/formaciones/centro/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(formacionService).obtenerPorCentro(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorCentro_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/formaciones/centro/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(formacionService);
    }

    // -----------------------------------------
    // GET /api/formaciones/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_admin_devuelve200() throws Exception {

        when(formacionService.obtenerPorId(1L))
                .thenReturn(crearFormacionResponse());

        mockMvc.perform(get("/api/formaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Desarrollo de Aplicaciones Web"));

        verify(formacionService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorId_profesor_devuelve200() throws Exception {

        when(formacionService.obtenerPorId(1L))
                .thenReturn(crearFormacionResponse());

        mockMvc.perform(get("/api/formaciones/1"))
                .andExpect(status().isOk());

        verify(formacionService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorId_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/formaciones/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(formacionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_inexistente_devuelve404() throws Exception {

        when(formacionService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Formación no encontrada"));

        mockMvc.perform(get("/api/formaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(formacionService).obtenerPorId(99L);
    }

    // -----------------------------------------
    // POST /api/formaciones
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_admin_devuelve201() throws Exception {

        when(formacionService.crear(any(FormacionRequest.class)))
                .thenReturn(crearFormacionResponse());

        mockMvc.perform(post("/api/formaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFormacion()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Desarrollo de Aplicaciones Web"));

        verify(formacionService).crear(any(FormacionRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void crear_profesor_devuelve403() throws Exception {

        mockMvc.perform(post("/api/formaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFormacion()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(formacionService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void crear_alumno_devuelve403() throws Exception {

        mockMvc.perform(post("/api/formaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFormacion()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(formacionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_nombreVacio_devuelve400() throws Exception {

        String json = """
                {
                    "nombre": "",
                    "descripcion": "Ciclo Formativo",
                    "nivel": "Grado Superior",
                    "codigo": "DAW",
                    "centroId": 1
                }
                """;

        mockMvc.perform(post("/api/formaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.nombre").exists());

        verifyNoInteractions(formacionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_centroIdNulo_devuelve400() throws Exception {

        String json = """
                {
                    "nombre": "Desarrollo de Aplicaciones Web",
                    "descripcion": "Ciclo Formativo",
                    "nivel": "Grado Superior",
                    "codigo": "DAW",
                    "centroId": null
                }
                """;

        mockMvc.perform(post("/api/formaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.centroId").exists());

        verifyNoInteractions(formacionService);
    }

    // -----------------------------------------
    // PUT /api/formaciones/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_admin_devuelve200() throws Exception {

        when(formacionService.actualizar(eq(1L), any(FormacionRequest.class)))
                .thenReturn(crearFormacionResponse());

        mockMvc.perform(put("/api/formaciones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFormacion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(formacionService).actualizar(eq(1L), any(FormacionRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void actualizar_profesor_devuelve403() throws Exception {

        mockMvc.perform(put("/api/formaciones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFormacion()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(formacionService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void actualizar_alumno_devuelve403() throws Exception {

        mockMvc.perform(put("/api/formaciones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFormacion()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(formacionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_inexistente_devuelve404() throws Exception {

        when(formacionService.actualizar(eq(99L), any(FormacionRequest.class)))
                .thenThrow(new ResourceNotFoundException("Formación no encontrada"));

        mockMvc.perform(put("/api/formaciones/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFormacion()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(formacionService).actualizar(eq(99L), any(FormacionRequest.class));
    }

    // -----------------------------------------
    // DELETE /api/formaciones/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_admin_devuelve204() throws Exception {

        doNothing().when(formacionService).eliminar(1L);

        mockMvc.perform(delete("/api/formaciones/1"))
                .andExpect(status().isNoContent());

        verify(formacionService).eliminar(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void eliminar_profesor_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/formaciones/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(formacionService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void eliminar_alumno_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/formaciones/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(formacionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_inexistente_devuelve404() throws Exception {

        doThrow(new ResourceNotFoundException("Formación no encontrada"))
                .when(formacionService).eliminar(99L);

        mockMvc.perform(delete("/api/formaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(formacionService).eliminar(99L);
    }

    // -----------------------------------------
    // AUTENTICACIÓN
    // -----------------------------------------

    @Test
    void obtenerTodos_sinAutenticacion_devuelve401() throws Exception {

        mockMvc.perform(get("/api/formaciones"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(formacionService);
    }
}

