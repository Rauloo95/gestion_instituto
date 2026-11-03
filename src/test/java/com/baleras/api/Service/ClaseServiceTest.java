package com.baleras.api.Service;

import com.baleras.api.Dto.ClaseRequest;
import com.baleras.api.Dto.ClaseResponse;
import com.baleras.api.Entity.Asignatura;
import com.baleras.api.Entity.Clase;
import com.baleras.api.Entity.Rol;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Exception.AccessDeniedException;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.AsignaturaRepository;
import com.baleras.api.Repository.ClaseRepository;
import com.baleras.api.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaseServiceTest {

    @Mock
    private ClaseRepository claseRepository;

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioActualService usuarioActualService;

    @InjectMocks
    private ClaseService claseService;

    private Usuario admin;
    private Usuario profesor;
    private Usuario otroProfesor;
    private Usuario alumno;

    private Asignatura asignatura;
    private Clase clase;

    @BeforeEach
    void setUp() {

        admin = Usuario.builder()
                .id(1L)
                .nombre("Admin")
                .apellidos("Principal")
                .email("admin@test.com")
                .rol(Rol.ADMIN)
                .build();

        profesor = Usuario.builder()
                .id(2L)
                .nombre("Carlos")
                .apellidos("García")
                .email("carlos@test.com")
                .rol(Rol.PROFESOR)
                .build();

        otroProfesor = Usuario.builder()
                .id(3L)
                .nombre("Ana")
                .apellidos("López")
                .email("ana@test.com")
                .rol(Rol.PROFESOR)
                .build();

        alumno = Usuario.builder()
                .id(4L)
                .nombre("Pedro")
                .apellidos("Martínez")
                .email("pedro@test.com")
                .rol(Rol.ALUMNO)
                .build();

        asignatura = Asignatura.builder()
                .id(1L)
                .nombre("Programación")
                .codigo("PROG")
                .curso("Primero")
                .build();

        clase = Clase.builder()
                .id(1L)
                .nombre("Grupo A")
                .grupo("A")
                .aula("101")
                .activo(true)
                .asignatura(asignatura)
                .profesor(profesor)
                .build();
    }

    // =========================
    // OBTENER TODAS
    // =========================

    @Test
    void obtenerTodos_deberiaDevolverTodasLasClases() {

        when(claseRepository.findAll()).thenReturn(List.of(clase));

        List<ClaseResponse> response = claseService.obtenerTodos();

        assertEquals(1, response.size());
        assertEquals("Grupo A", response.get(0).nombre());
        assertEquals("Programación", response.get(0).asignaturaNombre());
        assertEquals("Carlos", response.get(0).profesorNombre());

        verify(claseRepository).findAll();
    }

    @Test
    void obtenerTodos_sinClases_deberiaDevolverListaVacia() {

        when(claseRepository.findAll()).thenReturn(List.of());

        List<ClaseResponse> response = claseService.obtenerTodos();

        assertTrue(response.isEmpty());
    }

    // =========================
    // OBTENER POR ID
    // =========================

    @Test
    void obtenerPorId_admin_deberiaDevolverClase() {

        when(claseRepository.findById(1L)).thenReturn(Optional.of(clase));
        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(admin);

        ClaseResponse response = claseService.obtenerPorId(1L);

        assertEquals("Grupo A", response.nombre());
        assertEquals(1L, response.id());
    }

    @Test
    void obtenerPorId_profesorPropietario_deberiaDevolverClase() {

        when(claseRepository.findById(1L)).thenReturn(Optional.of(clase));
        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(profesor);

        ClaseResponse response = claseService.obtenerPorId(1L);

        assertEquals("Grupo A", response.nombre());
        assertEquals(2L, response.profesorId());
    }

    @Test
    void obtenerPorId_profesorDeOtraClase_deberiaLanzarAccessDeniedException() {

        when(claseRepository.findById(1L)).thenReturn(Optional.of(clase));
        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(otroProfesor);

        assertThrows(
                AccessDeniedException.class,
                () -> claseService.obtenerPorId(1L)
        );
    }

    @Test
    void obtenerPorId_alumno_deberiaLanzarAccessDeniedException() {

        when(claseRepository.findById(1L)).thenReturn(Optional.of(clase));
        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(alumno);

        assertThrows(
                AccessDeniedException.class,
                () -> claseService.obtenerPorId(1L)
        );
    }

    @Test
    void obtenerPorId_claseInexistente_deberiaLanzarResourceNotFoundException() {

        when(claseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claseService.obtenerPorId(99L)
        );

        verify(usuarioActualService, never()).obtenerUsuarioActual();
    }

    // =========================
    // OBTENER POR ASIGNATURA
    // =========================

    @Test
    void obtenerPorAsignatura_admin_deberiaDevolverTodasLasClases() {

        Clase otraClase = Clase.builder()
                .id(2L)
                .nombre("Grupo B")
                .grupo("B")
                .aula("102")
                .activo(true)
                .asignatura(asignatura)
                .profesor(otroProfesor)
                .build();

        when(asignaturaRepository.existsById(1L)).thenReturn(true);
        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(admin);
        when(claseRepository.findByAsignaturaId(1L))
                .thenReturn(List.of(clase, otraClase));

        List<ClaseResponse> response = claseService.obtenerPorAsignatura(1L);

        assertEquals(2, response.size());
    }

    @Test
    void obtenerPorAsignatura_profesor_deberiaDevolverSoloSusClases() {

        Clase otraClase = Clase.builder()
                .id(2L)
                .nombre("Grupo B")
                .grupo("B")
                .aula("102")
                .activo(true)
                .asignatura(asignatura)
                .profesor(otroProfesor)
                .build();

        when(asignaturaRepository.existsById(1L)).thenReturn(true);
        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(profesor);
        when(claseRepository.findByAsignaturaId(1L))
                .thenReturn(List.of(clase, otraClase));

        List<ClaseResponse> response = claseService.obtenerPorAsignatura(1L);

        assertEquals(1, response.size());
        assertEquals("Grupo A", response.get(0).nombre());
    }

    @Test
    void obtenerPorAsignatura_alumno_deberiaLanzarAccessDeniedException() {

        when(asignaturaRepository.existsById(1L)).thenReturn(true);
        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(alumno);
        when(claseRepository.findByAsignaturaId(1L)).thenReturn(List.of(clase));

        assertThrows(
                AccessDeniedException.class,
                () -> claseService.obtenerPorAsignatura(1L)
        );
    }

    @Test
    void obtenerPorAsignatura_inexistente_deberiaLanzarResourceNotFoundException() {

        when(asignaturaRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> claseService.obtenerPorAsignatura(99L)
        );
    }

    // =========================
    // OBTENER POR PROFESOR
    // =========================

    @Test
    void obtenerPorProfesor_admin_deberiaDevolverClases() {

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(admin);
        when(claseRepository.findByProfesorId(2L)).thenReturn(List.of(clase));

        List<ClaseResponse> response = claseService.obtenerPorProfesor(2L);

        assertEquals(1, response.size());
        assertEquals("Grupo A", response.get(0).nombre());
    }

    @Test
    void obtenerPorProfesor_profesorPropietario_deberiaDevolverClases() {

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(profesor);
        when(claseRepository.findByProfesorId(2L)).thenReturn(List.of(clase));

        List<ClaseResponse> response = claseService.obtenerPorProfesor(2L);

        assertEquals(1, response.size());
    }

    @Test
    void obtenerPorProfesor_profesorAjeno_deberiaLanzarAccessDeniedException() {

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(otroProfesor);

        assertThrows(
                AccessDeniedException.class,
                () -> claseService.obtenerPorProfesor(2L)
        );

        verify(claseRepository, never()).findByProfesorId(any());
    }

    @Test
    void obtenerPorProfesor_usuarioInexistente_deberiaLanzarResourceNotFoundException() {

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claseService.obtenerPorProfesor(99L)
        );
    }

    @Test
    void obtenerPorProfesor_usuarioQueNoEsProfesor_deberiaLanzarIllegalArgumentException() {

        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(alumno));

        assertThrows(
                IllegalArgumentException.class,
                () -> claseService.obtenerPorProfesor(4L)
        );

        verify(usuarioActualService, never()).obtenerUsuarioActual();
    }

    // =========================
    // CREAR
    // =========================

    @Test
    void crear_deberiaGuardarClaseCorrectamente() {

        ClaseRequest request = new ClaseRequest(
                "Grupo B",
                "B",
                "102",
                1L,
                2L
        );

        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(claseRepository.existsByNombreAndAsignaturaId("Grupo B", 1L))
                .thenReturn(false);

        when(claseRepository.save(any(Clase.class))).thenAnswer(invocation -> {
            Clase guardada = invocation.getArgument(0);
            guardada.setId(2L);
            return guardada;
        });

        ClaseResponse response = claseService.crear(request);

        assertEquals(2L, response.id());
        assertEquals("Grupo B", response.nombre());
        assertEquals("B", response.grupo());
        assertEquals("102", response.aula());
        assertTrue(response.activo());

        verify(claseRepository).save(any(Clase.class));
    }

    @Test
    void crear_asignaturaInexistente_deberiaLanzarResourceNotFoundException() {

        ClaseRequest request = new ClaseRequest(
                "Grupo B", "B", "102", 99L, 2L
        );

        when(asignaturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claseService.crear(request)
        );

        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void crear_profesorInexistente_deberiaLanzarResourceNotFoundException() {

        ClaseRequest request = new ClaseRequest(
                "Grupo B", "B", "102", 1L, 99L
        );

        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claseService.crear(request)
        );
    }

    @Test
    void crear_usuarioQueNoEsProfesor_deberiaLanzarIllegalArgumentException() {

        ClaseRequest request = new ClaseRequest(
                "Grupo B", "B", "102", 1L, 4L
        );

        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(alumno));

        assertThrows(
                IllegalArgumentException.class,
                () -> claseService.crear(request)
        );
    }

    @Test
    void crear_claseDuplicada_deberiaLanzarDuplicateResourceException() {

        ClaseRequest request = new ClaseRequest(
                "Grupo A", "A", "101", 1L, 2L
        );

        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(claseRepository.existsByNombreAndAsignaturaId("Grupo A", 1L))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> claseService.crear(request)
        );

        verify(claseRepository, never()).save(any());
    }

    // =========================
    // ACTUALIZAR
    // =========================

    @Test
    void actualizar_deberiaModificarClaseCorrectamente() {

        ClaseRequest request = new ClaseRequest(
                "Grupo C",
                "C",
                "103",
                1L,
                3L
        );

        when(claseRepository.findById(1L)).thenReturn(Optional.of(clase));
        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(otroProfesor));

        when(claseRepository.save(any(Clase.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaseResponse response = claseService.actualizar(1L, request);

        assertEquals("Grupo C", response.nombre());
        assertEquals("C", response.grupo());
        assertEquals("103", response.aula());
        assertEquals(3L, response.profesorId());

        verify(claseRepository).save(clase);
    }

    @Test
    void actualizar_claseInexistente_deberiaLanzarResourceNotFoundException() {

        ClaseRequest request = new ClaseRequest(
                "Grupo C", "C", "103", 1L, 2L
        );

        when(claseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claseService.actualizar(99L, request)
        );
    }

    @Test
    void actualizar_asignaturaInexistente_deberiaLanzarResourceNotFoundException() {

        ClaseRequest request = new ClaseRequest(
                "Grupo C", "C", "103", 99L, 2L
        );

        when(claseRepository.findById(1L)).thenReturn(Optional.of(clase));
        when(asignaturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claseService.actualizar(1L, request)
        );
    }

    @Test
    void actualizar_profesorInexistente_deberiaLanzarResourceNotFoundException() {

        ClaseRequest request = new ClaseRequest(
                "Grupo C", "C", "103", 1L, 99L
        );

        when(claseRepository.findById(1L)).thenReturn(Optional.of(clase));
        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claseService.actualizar(1L, request)
        );
    }

    @Test
    void actualizar_claseDuplicada_deberiaLanzarDuplicateResourceException() {

        ClaseRequest request = new ClaseRequest(
                "Grupo B", "B", "102", 1L, 2L
        );

        when(claseRepository.findById(1L)).thenReturn(Optional.of(clase));
        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(claseRepository.existsByNombreAndAsignaturaId("Grupo B", 1L))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> claseService.actualizar(1L, request)
        );

        verify(claseRepository, never()).save(any());
    }

    // =========================
    // ELIMINAR
    // =========================

    @Test
    void eliminar_deberiaEliminarClase() {

        when(claseRepository.findById(1L)).thenReturn(Optional.of(clase));

        claseService.eliminar(1L);

        verify(claseRepository).delete(clase);
    }

    @Test
    void eliminar_claseInexistente_deberiaLanzarResourceNotFoundException() {

        when(claseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> claseService.eliminar(99L)
        );

        verify(claseRepository, never()).delete(any());
    }
}
