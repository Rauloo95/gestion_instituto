package com.baleras.api.Controller;

import com.baleras.api.Dto.NotaRequest;
import com.baleras.api.Dto.NotaResponse;
import com.baleras.api.Exception.GlobalExceptionHandler;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.UsuarioRepository;
import com.baleras.api.Security.JwtAuthenticationFilter;
import com.baleras.api.Service.JwtService;
import com.baleras.api.Service.NotaService;

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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotaController.class)
@Import({
        TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
class NotaControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private NotaService notaService;

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

    private NotaResponse crearNotaResponse() {
        return new NotaResponse(
                1L,
                8.5,
                "Buen trabajo en el examen",
                LocalDateTime.now(),
                1L,
                3L,
                "Alumno Pérez",
                1L,
                "Programación 1A",
                1L,
                "Programación",
                2L,
                "Profesor Gómez"
        );
    }

    private String jsonNota() {
        return """
                {
                    "valor": 8.5,
                    "observaciones": "Buen trabajo en el examen",
                    "matriculaId": 1
                }
                """;
    }

    // -----------------------------------------
    // GET /api/notas
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerTodos_admin_devuelve200() throws Exception {

        when(notaService.obtenerTodos())
                .thenReturn(List.of(crearNotaResponse()));

        mockMvc.perform(get("/api/notas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].valor").value(8.5));

        verify(notaService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerTodos_profesor_devuelve200() throws Exception {

        when(notaService.obtenerTodos())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(notaService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerTodos_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/notas"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notaService);
    }

    // -----------------------------------------
    // GET /api/notas/mis-notas
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerMisNotas_alumno_devuelve200() throws Exception {

        when(notaService.obtenerMisNotas())
                .thenReturn(List.of(crearNotaResponse()));

        mockMvc.perform(get("/api/notas/mis-notas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].valor").value(8.5));

        verify(notaService).obtenerMisNotas();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerMisNotas_admin_devuelve403() throws Exception {

        mockMvc.perform(get("/api/notas/mis-notas"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notaService);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerMisNotas_profesor_devuelve403() throws Exception {

        mockMvc.perform(get("/api/notas/mis-notas"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notaService);
    }

    // -----------------------------------------
    // GET /api/notas/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_admin_devuelve200() throws Exception {

        when(notaService.obtenerPorId(1L))
                .thenReturn(crearNotaResponse());

        mockMvc.perform(get("/api/notas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valor").value(8.5));

        verify(notaService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorId_profesor_devuelve200() throws Exception {

        when(notaService.obtenerPorId(1L))
                .thenReturn(crearNotaResponse());

        mockMvc.perform(get("/api/notas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(notaService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorId_alumno_devuelve200() throws Exception {

        when(notaService.obtenerPorId(1L))
                .thenReturn(crearNotaResponse());

        mockMvc.perform(get("/api/notas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(notaService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_inexistente_devuelve404() throws Exception {

        when(notaService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Nota no encontrada"));

        mockMvc.perform(get("/api/notas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(notaService).obtenerPorId(99L);
    }

    // -----------------------------------------
    // GET /api/notas/matricula/{matriculaId}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorMatricula_admin_devuelve200() throws Exception {

        when(notaService.obtenerPorMatricula(1L))
                .thenReturn(crearNotaResponse());

        mockMvc.perform(get("/api/notas/matricula/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.matriculaId").value(1));

        verify(notaService).obtenerPorMatricula(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorMatricula_profesor_devuelve200() throws Exception {

        when(notaService.obtenerPorMatricula(1L))
                .thenReturn(crearNotaResponse());

        mockMvc.perform(get("/api/notas/matricula/1"))
                .andExpect(status().isOk());

        verify(notaService).obtenerPorMatricula(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorMatricula_alumno_devuelve200() throws Exception {

        when(notaService.obtenerPorMatricula(1L))
                .thenReturn(crearNotaResponse());

        mockMvc.perform(get("/api/notas/matricula/1"))
                .andExpect(status().isOk());

        verify(notaService).obtenerPorMatricula(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorMatricula_inexistente_devuelve404() throws Exception {

        when(notaService.obtenerPorMatricula(99L))
                .thenThrow(new ResourceNotFoundException("Nota no encontrada"));

        mockMvc.perform(get("/api/notas/matricula/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(notaService).obtenerPorMatricula(99L);
    }

    // -----------------------------------------
    // GET /api/notas/alumno/{alumnoId}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorAlumno_admin_devuelve200() throws Exception {

        when(notaService.obtenerPorAlumno(3L))
                .thenReturn(List.of(crearNotaResponse()));

        mockMvc.perform(get("/api/notas/alumno/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].alumnoId").value(3));

        verify(notaService).obtenerPorAlumno(3L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorAlumno_profesor_devuelve200() throws Exception {

        when(notaService.obtenerPorAlumno(3L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notas/alumno/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(notaService).obtenerPorAlumno(3L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorAlumno_alumno_devuelve200() throws Exception {

        when(notaService.obtenerPorAlumno(3L))
                .thenReturn(List.of(crearNotaResponse()));

        mockMvc.perform(get("/api/notas/alumno/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(notaService).obtenerPorAlumno(3L);
    }

    // -----------------------------------------
    // GET /api/notas/profesor/{profesorId}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorProfesor_admin_devuelve200() throws Exception {

        when(notaService.obtenerPorProfesor(2L))
                .thenReturn(List.of(crearNotaResponse()));

        mockMvc.perform(get("/api/notas/profesor/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].profesorId").value(2));

        verify(notaService).obtenerPorProfesor(2L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorProfesor_profesor_devuelve200() throws Exception {

        when(notaService.obtenerPorProfesor(2L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notas/profesor/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(notaService).obtenerPorProfesor(2L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorProfesor_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/notas/profesor/2"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notaService);
    }

    // -----------------------------------------
    // POST /api/notas
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "PROFESOR")
    void crear_profesor_devuelve201() throws Exception {

        when(notaService.crear(any(NotaRequest.class)))
                .thenReturn(crearNotaResponse());

        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNota()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valor").value(8.5));

        verify(notaService).crear(any(NotaRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_admin_devuelve403() throws Exception {

        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNota()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notaService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void crear_alumno_devuelve403() throws Exception {

        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNota()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notaService);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void crear_valorNulo_devuelve400() throws Exception {

        String json = """
                {
                    "valor": null,
                    "observaciones": "Nota nula",
                    "matriculaId": 1
                }
                """;

        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.valor").exists());

        verifyNoInteractions(notaService);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void crear_valorMenorQue0_devuelve400() throws Exception {

        String json = """
                {
                    "valor": -1.0,
                    "observaciones": "Nota negativa",
                    "matriculaId": 1
                }
                """;

        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.valor").exists());

        verifyNoInteractions(notaService);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void crear_valorMayorQue10_devuelve400() throws Exception {

        String json = """
                {
                    "valor": 10.5,
                    "observaciones": "Nota superior a 10",
                    "matriculaId": 1
                }
                """;

        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.valor").exists());

        verifyNoInteractions(notaService);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void crear_matriculaIdNulo_devuelve400() throws Exception {

        String json = """
                {
                    "valor": 7.0,
                    "observaciones": "Sin matrícula",
                    "matriculaId": null
                }
                """;

        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.matriculaId").exists());

        verifyNoInteractions(notaService);
    }

    // -----------------------------------------
    // PUT /api/notas/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "PROFESOR")
    void actualizar_profesor_devuelve200() throws Exception {

        when(notaService.actualizar(eq(1L), any(NotaRequest.class)))
                .thenReturn(crearNotaResponse());

        mockMvc.perform(put("/api/notas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNota()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(notaService).actualizar(eq(1L), any(NotaRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_admin_devuelve403() throws Exception {

        mockMvc.perform(put("/api/notas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNota()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notaService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void actualizar_alumno_devuelve403() throws Exception {

        mockMvc.perform(put("/api/notas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNota()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notaService);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void actualizar_inexistente_devuelve404() throws Exception {

        when(notaService.actualizar(eq(99L), any(NotaRequest.class)))
                .thenThrow(new ResourceNotFoundException("Nota no encontrada"));

        mockMvc.perform(put("/api/notas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNota()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(notaService).actualizar(eq(99L), any(NotaRequest.class));
    }

    // -----------------------------------------
    // DELETE /api/notas/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "PROFESOR")
    void eliminar_profesor_devuelve204() throws Exception {

        doNothing().when(notaService).eliminar(1L);

        mockMvc.perform(delete("/api/notas/1"))
                .andExpect(status().isNoContent());

        verify(notaService).eliminar(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_admin_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/notas/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notaService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void eliminar_alumno_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/notas/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notaService);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void eliminar_inexistente_devuelve404() throws Exception {

        doThrow(new ResourceNotFoundException("Nota no encontrada"))
                .when(notaService).eliminar(99L);

        mockMvc.perform(delete("/api/notas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(notaService).eliminar(99L);
    }

    // -----------------------------------------
    // AUTENTICACIÓN
    // -----------------------------------------

    @Test
    void obtenerTodos_sinAutenticacion_devuelve401() throws Exception {

        mockMvc.perform(get("/api/notas"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(notaService);
    }
}

