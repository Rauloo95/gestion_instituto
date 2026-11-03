package com.baleras.api.Controller;

import com.baleras.api.Dto.ClaseRequest;
import com.baleras.api.Dto.ClaseResponse;
import com.baleras.api.Entity.Rol;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Exception.GlobalExceptionHandler;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.UsuarioRepository;
import com.baleras.api.Security.JwtAuthenticationFilter;
import com.baleras.api.Service.ClaseService;
import com.baleras.api.Service.JwtService;
import com.baleras.api.Service.UsuarioActualService;

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

@WebMvcTest(ClaseController.class)
@Import({
        TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
class ClaseControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private ClaseService claseService;

    @MockitoBean
    private UsuarioActualService usuarioActualService;

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

    private ClaseResponse crearClaseResponse() {
        return new ClaseResponse(
                1L,
                "Programación 1A",
                "Grupo A",
                "Aula 101",
                true,
                1L,
                "Programación",
                2L,
                "Profesor Gómez"
        );
    }

    private String jsonClase() {
        return """
                {
                    "nombre": "Programación 1A",
                    "grupo": "Grupo A",
                    "aula": "Aula 101",
                    "asignaturaId": 1,
                    "profesorId": 2
                }
                """;
    }

    // -----------------------------------------
    // GET /api/clases
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerTodos_admin_devuelve200() throws Exception {

        when(claseService.obtenerTodos())
                .thenReturn(List.of(crearClaseResponse()));

        mockMvc.perform(get("/api/clases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Programación 1A"))
                .andExpect(jsonPath("$[0].grupo").value("Grupo A"));

        verify(claseService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerTodos_profesor_devuelve200() throws Exception {

        when(claseService.obtenerTodos())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/clases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(claseService).obtenerTodos();
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerTodos_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/clases"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claseService);
    }

    // -----------------------------------------
    // GET /api/clases/asignatura/{asignaturaId}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorAsignatura_admin_devuelve200() throws Exception {

        when(claseService.obtenerPorAsignatura(1L))
                .thenReturn(List.of(crearClaseResponse()));

        mockMvc.perform(get("/api/clases/asignatura/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].asignaturaId").value(1));

        verify(claseService).obtenerPorAsignatura(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorAsignatura_profesor_devuelve200() throws Exception {

        when(claseService.obtenerPorAsignatura(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/clases/asignatura/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(claseService).obtenerPorAsignatura(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorAsignatura_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/clases/asignatura/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claseService);
    }

    // -----------------------------------------
    // GET /api/clases/profesor/{profesorId}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorProfesor_admin_devuelve200() throws Exception {

        when(claseService.obtenerPorProfesor(2L))
                .thenReturn(List.of(crearClaseResponse()));

        mockMvc.perform(get("/api/clases/profesor/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].profesorId").value(2));

        verify(claseService).obtenerPorProfesor(2L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorProfesor_profesor_devuelve200() throws Exception {

        when(claseService.obtenerPorProfesor(2L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/clases/profesor/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(claseService).obtenerPorProfesor(2L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorProfesor_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/clases/profesor/2"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claseService);
    }

    // -----------------------------------------
    // GET /api/clases/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_admin_devuelve200() throws Exception {

        when(claseService.obtenerPorId(1L))
                .thenReturn(crearClaseResponse());

        mockMvc.perform(get("/api/clases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Programación 1A"));

        verify(claseService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerPorId_profesor_devuelve200() throws Exception {

        when(claseService.obtenerPorId(1L))
                .thenReturn(crearClaseResponse());

        mockMvc.perform(get("/api/clases/1"))
                .andExpect(status().isOk());

        verify(claseService).obtenerPorId(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerPorId_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/clases/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claseService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_inexistente_devuelve404() throws Exception {

        when(claseService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Clase no encontrada"));

        mockMvc.perform(get("/api/clases/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(claseService).obtenerPorId(99L);
    }

    // -----------------------------------------
    // POST /api/clases
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_admin_devuelve201() throws Exception {

        when(claseService.crear(any(ClaseRequest.class)))
                .thenReturn(crearClaseResponse());

        mockMvc.perform(post("/api/clases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonClase()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Programación 1A"));

        verify(claseService).crear(any(ClaseRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void crear_profesor_devuelve403() throws Exception {

        mockMvc.perform(post("/api/clases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonClase()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claseService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void crear_alumno_devuelve403() throws Exception {

        mockMvc.perform(post("/api/clases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonClase()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claseService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_nombreVacio_devuelve400() throws Exception {

        String json = """
                {
                    "nombre": "",
                    "grupo": "Grupo A",
                    "aula": "Aula 101",
                    "asignaturaId": 1,
                    "profesorId": 2
                }
                """;

        mockMvc.perform(post("/api/clases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.nombre").exists());

        verifyNoInteractions(claseService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_asignaturaIdNulo_devuelve400() throws Exception {

        String json = """
                {
                    "nombre": "Programación 1A",
                    "grupo": "Grupo A",
                    "aula": "Aula 101",
                    "asignaturaId": null,
                    "profesorId": 2
                }
                """;

        mockMvc.perform(post("/api/clases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.asignaturaId").exists());

        verifyNoInteractions(claseService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_profesorIdNulo_devuelve400() throws Exception {

        String json = """
                {
                    "nombre": "Programación 1A",
                    "grupo": "Grupo A",
                    "aula": "Aula 101",
                    "asignaturaId": 1,
                    "profesorId": null
                }
                """;

        mockMvc.perform(post("/api/clases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.profesorId").exists());

        verifyNoInteractions(claseService);
    }

    // -----------------------------------------
    // PUT /api/clases/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_admin_devuelve200() throws Exception {

        when(claseService.actualizar(eq(1L), any(ClaseRequest.class)))
                .thenReturn(crearClaseResponse());

        mockMvc.perform(put("/api/clases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonClase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(claseService).actualizar(eq(1L), any(ClaseRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void actualizar_profesor_devuelve403() throws Exception {

        mockMvc.perform(put("/api/clases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonClase()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claseService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void actualizar_alumno_devuelve403() throws Exception {

        mockMvc.perform(put("/api/clases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonClase()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claseService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_inexistente_devuelve404() throws Exception {

        when(claseService.actualizar(eq(99L), any(ClaseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Clase no encontrada"));

        mockMvc.perform(put("/api/clases/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonClase()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(claseService).actualizar(eq(99L), any(ClaseRequest.class));
    }

    // -----------------------------------------
    // DELETE /api/clases/{id}
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_admin_devuelve204() throws Exception {

        doNothing().when(claseService).eliminar(1L);

        mockMvc.perform(delete("/api/clases/1"))
                .andExpect(status().isNoContent());

        verify(claseService).eliminar(1L);
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    void eliminar_profesor_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/clases/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claseService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void eliminar_alumno_devuelve403() throws Exception {

        mockMvc.perform(delete("/api/clases/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claseService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_inexistente_devuelve404() throws Exception {

        doThrow(new ResourceNotFoundException("Clase no encontrada"))
                .when(claseService).eliminar(99L);

        mockMvc.perform(delete("/api/clases/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(claseService).eliminar(99L);
    }

    // -----------------------------------------
    // GET /api/clases/mis-clases
    // -----------------------------------------

    @Test
    @WithMockUser(roles = "PROFESOR")
    void obtenerMisClases_profesor_devuelve200() throws Exception {

        Usuario usuario = Usuario.builder()
                .id(2L)
                .nombre("Profesor")
                .apellidos("Gómez")
                .email("profe@test.com")
                .rol(Rol.PROFESOR)
                .build();

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(usuario);
        when(claseService.obtenerPorProfesor(2L))
                .thenReturn(List.of(crearClaseResponse()));

        mockMvc.perform(get("/api/clases/mis-clases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].profesorId").value(2));

        verify(usuarioActualService).obtenerUsuarioActual();
        verify(claseService).obtenerPorProfesor(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerMisClases_admin_devuelve403() throws Exception {

        mockMvc.perform(get("/api/clases/mis-clases"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioActualService);
        verifyNoInteractions(claseService);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    void obtenerMisClases_alumno_devuelve403() throws Exception {

        mockMvc.perform(get("/api/clases/mis-clases"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioActualService);
        verifyNoInteractions(claseService);
    }

    // -----------------------------------------
    // AUTENTICACIÓN
    // -----------------------------------------

    @Test
    void obtenerTodos_sinAutenticacion_devuelve401() throws Exception {

        mockMvc.perform(get("/api/clases"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(claseService);
    }
}

