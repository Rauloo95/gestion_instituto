package com.baleras.api.Controller;

import com.baleras.api.Dto.AsignaturaRequest;
import com.baleras.api.Dto.AsignaturaResponse;
import com.baleras.api.Exception.GlobalExceptionHandler;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.UsuarioRepository;
import com.baleras.api.Security.JwtAuthenticationFilter;
import com.baleras.api.Service.AsignaturaService;
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

@WebMvcTest(AsignaturaController.class)
@Import({
        TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
class AsignaturaControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private AsignaturaService asignaturaService;

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

    private AsignaturaResponse crearAsignaturaResponse() {
        return new AsignaturaResponse(
                1L,
                "Programación",
                "Fundamentos de programación en Java",
                "PROG",
                "1º",
                1L,
                "Desarrollo de Aplicaciones Web"
        );
    }

    private String jsonAsignatura() {
        return """
                {
                    "nombre": "Programación",
                    "descripcion": "Fundamentos de programación en Java",
                    "codigo": "PROG",
                    "curso": "1º",
                    "formacionId": 1
                }
                """;
    }

    // -----------------------------------------
    // GET /api/asignaturas
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerTodos_admin_devuelve200() throws Exception {

        when(asignaturaService.obtenerTodos())
                .thenReturn(List.of(crearAsignaturaResponse()));

        mockMvc.perform(get("/api/asignaturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Programación"))
                .andExpect(jsonPath("$[0].codigo").value("PROG"));

        verify(asignaturaService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerTodos_profesor_devuelve200() throws Exception {

        when(asignaturaService.obtenerTodos())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/asignaturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(asignaturaService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerTodos_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/asignaturas"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(asignaturaService);
    }

    // -----------------------------------------
    // GET /api/asignaturas/formacion/{formacionId}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorFormacion_admin_devuelve200() throws Exception {

        when(asignaturaService.obtenerPorFormacion(1L))
                .thenReturn(List.of(crearAsignaturaResponse()));

        mockMvc.perform(get("/api/asignaturas/formacion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].formacionId").value(1));

        verify(asignaturaService).obtenerPorFormacion(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorFormacion_profesor_devuelve200() throws Exception {

        when(asignaturaService.obtenerPorFormacion(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/asignaturas/formacion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(asignaturaService).obtenerPorFormacion(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorFormacion_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/asignaturas/formacion/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(asignaturaService);
    }

    // -----------------------------------------
    // GET /api/asignaturas/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_admin_devuelve200() throws Exception {

        when(asignaturaService.obtenerPorId(1L))
                .thenReturn(crearAsignaturaResponse());

        mockMvc.perform(get("/api/asignaturas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Programación"));

        verify(asignaturaService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorId_profesor_devuelve200() throws Exception {

        when(asignaturaService.obtenerPorId(1L))
                .thenReturn(crearAsignaturaResponse());

        mockMvc.perform(get("/api/asignaturas/1"))
                .andExpect(status().isOk());

        verify(asignaturaService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorId_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/asignaturas/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(asignaturaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_inexistente_devuelve404() throws Exception {

        when(asignaturaService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Asignatura no encontrada"));

        mockMvc.perform(get("/api/asignaturas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(asignaturaService).obtenerPorId(99L);
    }

    // -----------------------------------------
    // POST /api/asignaturas
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_admin_devuelve201() throws Exception {

        when(asignaturaService.crear(any(AsignaturaRequest.class)))
                .thenReturn(crearAsignaturaResponse());

        mockMvc.perform(post("/api/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAsignatura()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Programación"));

        verify(asignaturaService).crear(any(AsignaturaRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void crear_profesor_devuelve403() throws Exception {

        mockMvc.perform(post("/api/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAsignatura()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(asignaturaService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void crear_alumno_devuelve403() throws Exception {

        mockMvc.perform(post("/api/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAsignatura()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(asignaturaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_nombreVacio_devuelve400() throws Exception {

        String json = """
                {
                    "nombre": "",
                    "descripcion": "Fundamentos",
                    "codigo": "PROG",
                    "curso": "1º",
                    "formacionId": 1
                }
                """;

        mockMvc.perform(post("/api/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.nombre").exists());

        verifyNoInteractions(asignaturaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_formacionIdNulo_devuelve400() throws Exception {

        String json = """
                {
                    "nombre": "Programación",
                    "descripcion": "Fundamentos",
                    "codigo": "PROG",
                    "curso": "1º",
                    "formacionId": null
                }
                """;

        mockMvc.perform(post("/api/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.formacionId").exists());

        verifyNoInteractions(asignaturaService);
    }

    // -----------------------------------------
    // PUT /api/asignaturas/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_admin_devuelve200() throws Exception {

        when(asignaturaService.actualizar(eq(1L), any(AsignaturaRequest.class)))
                .thenReturn(crearAsignaturaResponse());

        mockMvc.perform(put("/api/asignaturas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAsignatura()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(asignaturaService).actualizar(eq(1L), any(AsignaturaRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void actualizar_profesor_devuelve403() throws Exception {

        mockMvc.perform(put("/api/asignaturas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAsignatura()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(asignaturaService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void actualizar_alumno_devuelve403() throws Exception {

        mockMvc.perform(put("/api/asignaturas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAsignatura()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(asignaturaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_inexistente_devuelve404() throws Exception {

        when(asignaturaService.actualizar(eq(99L), any(AsignaturaRequest.class)))
                .thenThrow(new ResourceNotFoundException("Asignatura no encontrada"));

        mockMvc.perform(put("/api/asignaturas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAsignatura()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(asignaturaService).actualizar(eq(99L), any(AsignaturaRequest.class));
    }

    // -----------------------------------------
    // DELETE /api/asignaturas/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_admin_devuelve204() throws Exception {

        doNothing().when(asignaturaService).eliminar(1L);

        mockMvc.perform(delete("/api/asignaturas/1"))
                .andExpect(status().isNoContent());

        verify(asignaturaService).eliminar(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void eliminar_profesor_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/asignaturas/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(asignaturaService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void eliminar_alumno_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/asignaturas/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(asignaturaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_inexistente_devuelve404() throws Exception {

        doThrow(new ResourceNotFoundException("Asignatura no encontrada"))
                .when(asignaturaService).eliminar(99L);

        mockMvc.perform(delete("/api/asignaturas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(asignaturaService).eliminar(99L);
    }

    // -----------------------------------------
    // AUTENTICACIÓN
    // -----------------------------------------

    @Test
    void obtenerTodos_sinAutenticacion_devuelve401() throws Exception {

        mockMvc.perform(get("/api/asignaturas"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(asignaturaService);
    }
}

