package com.baleras.api.Controller;

import com.baleras.api.Dto.MatriculaRequest;
import com.baleras.api.Dto.MatriculaResponse;
import com.baleras.api.Entity.EstadoMatricula;
import com.baleras.api.Exception.GlobalExceptionHandler;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.UsuarioRepository;
import com.baleras.api.Security.JwtAuthenticationFilter;
import com.baleras.api.Service.JwtService;
import com.baleras.api.Service.MatriculaService;

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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatriculaController.class)
@Import({
        TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
class MatriculaControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private MatriculaService matriculaService;

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

    private MatriculaResponse crearMatriculaResponse() {
        return new MatriculaResponse(
                1L,
                3L,
                "Alumno Pérez",
                1L,
                "Programación 1A",
                1L,
                "Programación",
                LocalDate.now(),
                EstadoMatricula.ACTIVA
        );
    }

    private String jsonMatricula() {
        return """
                {
                    "alumnoId": 3,
                    "claseId": 1,
                    "estado": "ACTIVA"
                }
                """;
    }

    // -----------------------------------------
    // GET /api/matriculas
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerTodos_admin_devuelve200() throws Exception {

        when(matriculaService.obtenerTodos())
                .thenReturn(List.of(crearMatriculaResponse()));

        mockMvc.perform(get("/api/matriculas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].alumnoNombre").value("Alumno Pérez"))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"));

        verify(matriculaService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerTodos_profesor_devuelve200() throws Exception {

        when(matriculaService.obtenerTodos())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/matriculas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(matriculaService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerTodos_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/matriculas"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matriculaService);
    }

    // -----------------------------------------
    // GET /api/matriculas/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_admin_devuelve200() throws Exception {

        when(matriculaService.obtenerPorId(1L))
                .thenReturn(crearMatriculaResponse());

        mockMvc.perform(get("/api/matriculas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alumnoNombre").value("Alumno Pérez"));

        verify(matriculaService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorId_profesor_devuelve200() throws Exception {

        when(matriculaService.obtenerPorId(1L))
                .thenReturn(crearMatriculaResponse());

        mockMvc.perform(get("/api/matriculas/1"))
                .andExpect(status().isOk());

        verify(matriculaService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorId_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/matriculas/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matriculaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_inexistente_devuelve404() throws Exception {

        when(matriculaService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Matrícula no encontrada"));

        mockMvc.perform(get("/api/matriculas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(matriculaService).obtenerPorId(99L);
    }

    // -----------------------------------------
    // GET /api/matriculas/alumno/{alumnoId}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorAlumno_admin_devuelve200() throws Exception {

        when(matriculaService.obtenerPorAlumno(3L))
                .thenReturn(List.of(crearMatriculaResponse()));

        mockMvc.perform(get("/api/matriculas/alumno/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].alumnoId").value(3));

        verify(matriculaService).obtenerPorAlumno(3L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorAlumno_profesor_devuelve200() throws Exception {

        when(matriculaService.obtenerPorAlumno(3L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/matriculas/alumno/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(matriculaService).obtenerPorAlumno(3L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorAlumno_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/matriculas/alumno/3"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matriculaService);
    }

    // -----------------------------------------
    // GET /api/matriculas/clase/{claseId}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorClase_admin_devuelve200() throws Exception {

        when(matriculaService.obtenerPorClase(1L))
                .thenReturn(List.of(crearMatriculaResponse()));

        mockMvc.perform(get("/api/matriculas/clase/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].claseId").value(1));

        verify(matriculaService).obtenerPorClase(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorClase_profesor_devuelve200() throws Exception {

        when(matriculaService.obtenerPorClase(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/matriculas/clase/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(matriculaService).obtenerPorClase(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorClase_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/matriculas/clase/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matriculaService);
    }

    // -----------------------------------------
    // POST /api/matriculas
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_admin_devuelve201() throws Exception {

        when(matriculaService.crear(any(MatriculaRequest.class)))
                .thenReturn(crearMatriculaResponse());

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMatricula()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alumnoId").value(3))
                .andExpect(jsonPath("$.claseId").value(1));

        verify(matriculaService).crear(any(MatriculaRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void crear_profesor_devuelve403() throws Exception {

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMatricula()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matriculaService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void crear_alumno_devuelve403() throws Exception {

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMatricula()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matriculaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_alumnoIdNulo_devuelve400() throws Exception {

        String json = """
                {
                    "alumnoId": null,
                    "claseId": 1,
                    "estado": "ACTIVA"
                }
                """;

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.alumnoId").exists());

        verifyNoInteractions(matriculaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_claseIdNulo_devuelve400() throws Exception {

        String json = """
                {
                    "alumnoId": 3,
                    "claseId": null,
                    "estado": "ACTIVA"
                }
                """;

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.claseId").exists());

        verifyNoInteractions(matriculaService);
    }

    // -----------------------------------------
    // PUT /api/matriculas/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_admin_devuelve200() throws Exception {

        when(matriculaService.actualizar(eq(1L), any(MatriculaRequest.class)))
                .thenReturn(crearMatriculaResponse());

        mockMvc.perform(put("/api/matriculas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMatricula()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(matriculaService).actualizar(eq(1L), any(MatriculaRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void actualizar_profesor_devuelve403() throws Exception {

        mockMvc.perform(put("/api/matriculas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMatricula()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matriculaService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void actualizar_alumno_devuelve403() throws Exception {

        mockMvc.perform(put("/api/matriculas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMatricula()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matriculaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_inexistente_devuelve404() throws Exception {

        when(matriculaService.actualizar(eq(99L), any(MatriculaRequest.class)))
                .thenThrow(new ResourceNotFoundException("Matrícula no encontrada"));

        mockMvc.perform(put("/api/matriculas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMatricula()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(matriculaService).actualizar(eq(99L), any(MatriculaRequest.class));
    }

    // -----------------------------------------
    // DELETE /api/matriculas/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_admin_devuelve204() throws Exception {

        doNothing().when(matriculaService).eliminar(1L);

        mockMvc.perform(delete("/api/matriculas/1"))
                .andExpect(status().isNoContent());

        verify(matriculaService).eliminar(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void eliminar_profesor_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/matriculas/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matriculaService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void eliminar_alumno_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/matriculas/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(matriculaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_inexistente_devuelve404() throws Exception {

        doThrow(new ResourceNotFoundException("Matrícula no encontrada"))
                .when(matriculaService).eliminar(99L);

        mockMvc.perform(delete("/api/matriculas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(matriculaService).eliminar(99L);
    }

    // -----------------------------------------
    // AUTENTICACIÓN
    // -----------------------------------------

    @Test
    void obtenerTodos_sinAutenticacion_devuelve401() throws Exception {

        mockMvc.perform(get("/api/matriculas"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(matriculaService);
    }
}

