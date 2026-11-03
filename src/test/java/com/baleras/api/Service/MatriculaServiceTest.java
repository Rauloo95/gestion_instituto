package com.baleras.api.Service;

import com.baleras.api.Dto.MatriculaRequest;
import com.baleras.api.Dto.MatriculaResponse;
import com.baleras.api.Entity.*;
import com.baleras.api.Exception.AccessDeniedException;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.ClaseRepository;
import com.baleras.api.Repository.MatriculaRepository;
import com.baleras.api.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClaseRepository claseRepository;

    @Mock
    private UsuarioActualService usuarioActualService;

    @InjectMocks
    private MatriculaService matriculaService;

    private Usuario admin;
    private Usuario profesor;
    private Usuario otroProfesor;
    private Usuario alumno;
    private Usuario otroAlumno;

    private Asignatura asignatura;
    private Clase clase;
    private Matricula matricula;

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

        otroAlumno = Usuario.builder()
                .id(5L)
                .nombre("Luis")
                .apellidos("Sánchez")
                .email("luis@test.com")
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

        matricula = Matricula.builder()
                .id(1L)
                .alumno(alumno)
                .clase(clase)
                .fechaMatricula(LocalDate.now())
                .estado(EstadoMatricula.ACTIVA)
                .build();
    }

    // =========================
    // OBTENER TODAS
    // =========================

    @Test
    void obtenerTodos_deberiaDevolverTodasLasMatriculas() {

        when(matriculaRepository.findAll())
                .thenReturn(List.of(matricula));

        List<MatriculaResponse> response = matriculaService.obtenerTodos();

        assertEquals(1, response.size());
        assertEquals("Pedro", response.get(0).alumnoNombre());
        assertEquals("Grupo A", response.get(0).claseNombre());
        assertEquals("Programación", response.get(0).asignaturaNombre());
        assertEquals(EstadoMatricula.ACTIVA, response.get(0).estado());
    }

    @Test
    void obtenerTodos_sinMatriculas_deberiaDevolverListaVacia() {

        when(matriculaRepository.findAll()).thenReturn(List.of());

        List<MatriculaResponse> response = matriculaService.obtenerTodos();

        assertTrue(response.isEmpty());
    }

    // =========================
    // OBTENER POR ID
    // =========================

    @Test
    void obtenerPorId_deberiaDevolverMatricula() {

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        MatriculaResponse response = matriculaService.obtenerPorId(1L);

        assertEquals(1L, response.id());
        assertEquals(4L, response.alumnoId());
        assertEquals("Pedro", response.alumnoNombre());
        assertEquals(1L, response.claseId());
        assertEquals(LocalDate.now(), response.fechaMatricula());
    }

    @Test
    void obtenerPorId_matriculaInexistente_deberiaLanzarResourceNotFoundException() {

        when(matriculaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> matriculaService.obtenerPorId(99L)
        );
    }

    // =========================
    // OBTENER POR CLASE
    // =========================

    @Test
    void obtenerPorClase_admin_deberiaDevolverMatriculas() {

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(admin);

        when(matriculaRepository.findByClaseId(1L))
                .thenReturn(List.of(matricula));

        List<MatriculaResponse> response =
                matriculaService.obtenerPorClase(1L);

        assertEquals(1, response.size());
        assertEquals("Pedro", response.get(0).alumnoNombre());
    }

    @Test
    void obtenerPorClase_profesorPropietario_deberiaDevolverMatriculas() {

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        when(matriculaRepository.findByClaseId(1L))
                .thenReturn(List.of(matricula));

        List<MatriculaResponse> response =
                matriculaService.obtenerPorClase(1L);

        assertEquals(1, response.size());
    }

    @Test
    void obtenerPorClase_profesorAjeno_deberiaLanzarAccessDeniedException() {

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(otroProfesor);

        assertThrows(
                AccessDeniedException.class,
                () -> matriculaService.obtenerPorClase(1L)
        );

        verify(matriculaRepository, never()).findByClaseId(any());
    }

    @Test
    void obtenerPorClase_alumno_deberiaLanzarAccessDeniedException() {

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(alumno);

        assertThrows(
                AccessDeniedException.class,
                () -> matriculaService.obtenerPorClase(1L)
        );
    }

    @Test
    void obtenerPorClase_claseInexistente_deberiaLanzarResourceNotFoundException() {

        when(claseRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> matriculaService.obtenerPorClase(99L)
        );

        verify(usuarioActualService, never()).obtenerUsuarioActual();
    }

    // =========================
    // CREAR
    // =========================

    @Test
    void crear_deberiaGuardarMatriculaCorrectamente() {

        MatriculaRequest request = new MatriculaRequest(
                4L,
                1L,
                EstadoMatricula.ACTIVA
        );

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(matriculaRepository.existsByAlumnoIdAndClaseId(4L, 1L))
                .thenReturn(false);

        when(matriculaRepository.save(any(Matricula.class)))
                .thenAnswer(invocation -> {
                    Matricula guardada = invocation.getArgument(0);
                    guardada.setId(2L);
                    return guardada;
                });

        MatriculaResponse response = matriculaService.crear(request);

        assertEquals(2L, response.id());
        assertEquals("Pedro", response.alumnoNombre());
        assertEquals("Grupo A", response.claseNombre());
        assertEquals(EstadoMatricula.ACTIVA, response.estado());
        assertEquals(LocalDate.now(), response.fechaMatricula());

        verify(matriculaRepository).save(any(Matricula.class));
    }

    @Test
    void crear_sinEstado_deberiaAsignarEstadoActivo() {

        MatriculaRequest request = new MatriculaRequest(
                4L, 1L, null
        );

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(matriculaRepository.existsByAlumnoIdAndClaseId(4L, 1L))
                .thenReturn(false);

        when(matriculaRepository.save(any(Matricula.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatriculaResponse response = matriculaService.crear(request);

        assertEquals(EstadoMatricula.ACTIVA, response.estado());
    }

    @Test
    void crear_conEstadoPersonalizado_deberiaRespetarEstado() {

        MatriculaRequest request = new MatriculaRequest(
                4L, 1L, EstadoMatricula.FINALIZADA
        );

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(matriculaRepository.existsByAlumnoIdAndClaseId(4L, 1L))
                .thenReturn(false);

        when(matriculaRepository.save(any(Matricula.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatriculaResponse response = matriculaService.crear(request);

        assertEquals(EstadoMatricula.FINALIZADA, response.estado());
    }

    @Test
    void crear_alumnoInexistente_deberiaLanzarResourceNotFoundException() {

        MatriculaRequest request = new MatriculaRequest(
                99L, 1L, EstadoMatricula.ACTIVA
        );

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> matriculaService.crear(request)
        );

        verify(claseRepository, never()).findById(any());
    }

    @Test
    void crear_usuarioQueNoEsAlumno_deberiaLanzarIllegalArgumentException() {

        MatriculaRequest request = new MatriculaRequest(
                2L, 1L, EstadoMatricula.ACTIVA
        );

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(profesor));

        assertThrows(
                IllegalArgumentException.class,
                () -> matriculaService.crear(request)
        );
    }

    @Test
    void crear_claseInexistente_deberiaLanzarResourceNotFoundException() {

        MatriculaRequest request = new MatriculaRequest(
                4L, 99L, EstadoMatricula.ACTIVA
        );

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(claseRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> matriculaService.crear(request)
        );
    }

    @Test
    void crear_claseInactiva_deberiaLanzarIllegalArgumentException() {

        clase.setActivo(false);

        MatriculaRequest request = new MatriculaRequest(
                4L, 1L, EstadoMatricula.ACTIVA
        );

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        assertThrows(
                IllegalArgumentException.class,
                () -> matriculaService.crear(request)
        );

        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void crear_matriculaDuplicada_deberiaLanzarDuplicateResourceException() {

        MatriculaRequest request = new MatriculaRequest(
                4L, 1L, EstadoMatricula.ACTIVA
        );

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(matriculaRepository.existsByAlumnoIdAndClaseId(4L, 1L))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> matriculaService.crear(request)
        );

        verify(matriculaRepository, never()).save(any());
    }

    // =========================
    // ACTUALIZAR
    // =========================

    @Test
    void actualizar_deberiaModificarMatriculaCorrectamente() {

        MatriculaRequest request = new MatriculaRequest(
                5L,
                1L,
                EstadoMatricula.FINALIZADA
        );

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioRepository.findById(5L))
                .thenReturn(Optional.of(otroAlumno));

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(matriculaRepository.existsByAlumnoIdAndClaseId(5L, 1L))
                .thenReturn(false);

        when(matriculaRepository.save(any(Matricula.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatriculaResponse response =
                matriculaService.actualizar(1L, request);

        assertEquals(5L, response.alumnoId());
        assertEquals("Luis", response.alumnoNombre());
        assertEquals(EstadoMatricula.FINALIZADA, response.estado());

        verify(matriculaRepository).save(matricula);
    }

    @Test
    void actualizar_sinNuevoEstado_deberiaMantenerEstadoAnterior() {

        matricula.setEstado(EstadoMatricula.CANCELADA);

        MatriculaRequest request = new MatriculaRequest(
                4L, 1L, null
        );

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(matriculaRepository.save(any(Matricula.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatriculaResponse response =
                matriculaService.actualizar(1L, request);

        assertEquals(EstadoMatricula.CANCELADA, response.estado());

        verify(matriculaRepository, never())
                .existsByAlumnoIdAndClaseId(any(), any());
    }

    @Test
    void actualizar_matriculaInexistente_deberiaLanzarResourceNotFoundException() {

        MatriculaRequest request = new MatriculaRequest(
                4L, 1L, EstadoMatricula.ACTIVA
        );

        when(matriculaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> matriculaService.actualizar(99L, request)
        );

        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void actualizar_alumnoInexistente_deberiaLanzarResourceNotFoundException() {

        MatriculaRequest request = new MatriculaRequest(
                99L, 1L, EstadoMatricula.ACTIVA
        );

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> matriculaService.actualizar(1L, request)
        );
    }

    @Test
    void actualizar_usuarioQueNoEsAlumno_deberiaLanzarIllegalArgumentException() {

        MatriculaRequest request = new MatriculaRequest(
                2L, 1L, EstadoMatricula.ACTIVA
        );

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(profesor));

        assertThrows(
                IllegalArgumentException.class,
                () -> matriculaService.actualizar(1L, request)
        );
    }

    @Test
    void actualizar_claseInexistente_deberiaLanzarResourceNotFoundException() {

        MatriculaRequest request = new MatriculaRequest(
                4L, 99L, EstadoMatricula.ACTIVA
        );

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(claseRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> matriculaService.actualizar(1L, request)
        );
    }

    @Test
    void actualizar_matriculaDuplicada_deberiaLanzarDuplicateResourceException() {

        MatriculaRequest request = new MatriculaRequest(
                5L, 1L, EstadoMatricula.ACTIVA
        );

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioRepository.findById(5L))
                .thenReturn(Optional.of(otroAlumno));

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        when(matriculaRepository.existsByAlumnoIdAndClaseId(5L, 1L))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> matriculaService.actualizar(1L, request)
        );

        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void actualizar_claseInactiva_deberiaLanzarIllegalArgumentException() {

        clase.setActivo(false);

        MatriculaRequest request = new MatriculaRequest(
                4L, 1L, EstadoMatricula.ACTIVA
        );

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(claseRepository.findById(1L))
                .thenReturn(Optional.of(clase));

        assertThrows(
                IllegalArgumentException.class,
                () -> matriculaService.actualizar(1L, request)
        );
    }

    // =========================
    // ELIMINAR
    // =========================

    @Test
    void eliminar_deberiaEliminarMatricula() {

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        matriculaService.eliminar(1L);

        verify(matriculaRepository).delete(matricula);
    }

    @Test
    void eliminar_matriculaInexistente_deberiaLanzarResourceNotFoundException() {

        when(matriculaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> matriculaService.eliminar(99L)
        );

        verify(matriculaRepository, never()).delete(any());
    }

    // =========================
    // OBTENER POR ALUMNO
    // =========================

    @Test
    void obtenerPorAlumno_admin_deberiaDevolverMatriculas() {

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(admin);

        when(matriculaRepository.findByAlumnoId(4L))
                .thenReturn(List.of(matricula));

        List<MatriculaResponse> response =
                matriculaService.obtenerPorAlumno(4L);

        assertEquals(1, response.size());
        assertEquals("Pedro", response.get(0).alumnoNombre());
    }

    @Test
    void obtenerPorAlumno_profesorConClaseDelAlumno_deberiaDevolverMatriculas() {

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        when(matriculaRepository.findByAlumnoId(4L))
                .thenReturn(List.of(matricula));

        List<MatriculaResponse> response =
                matriculaService.obtenerPorAlumno(4L);

        assertEquals(1, response.size());

        verify(matriculaRepository, times(2))
                .findByAlumnoId(4L);
    }

    @Test
    void obtenerPorAlumno_profesorSinClaseDelAlumno_deberiaLanzarAccessDeniedException() {

        Matricula matriculaOtroProfesor = Matricula.builder()
                .id(2L)
                .alumno(alumno)
                .clase(Clase.builder()
                        .id(2L)
                        .nombre("Grupo B")
                        .activo(true)
                        .asignatura(asignatura)
                        .profesor(otroProfesor)
                        .build())
                .fechaMatricula(LocalDate.now())
                .estado(EstadoMatricula.ACTIVA)
                .build();

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        when(matriculaRepository.findByAlumnoId(4L))
                .thenReturn(List.of(matriculaOtroProfesor));

        assertThrows(
                AccessDeniedException.class,
                () -> matriculaService.obtenerPorAlumno(4L)
        );
    }

    @Test
    void obtenerPorAlumno_alumnoPropietario_deberiaDevolverMatriculas() {

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(alumno);

        when(matriculaRepository.findByAlumnoId(4L))
                .thenReturn(List.of(matricula));

        List<MatriculaResponse> response =
                matriculaService.obtenerPorAlumno(4L);

        assertEquals(1, response.size());
    }

    @Test
    void obtenerPorAlumno_alumnoAjeno_deberiaLanzarAccessDeniedException() {

        when(usuarioRepository.findById(5L))
                .thenReturn(Optional.of(otroAlumno));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(alumno);

        assertThrows(
                AccessDeniedException.class,
                () -> matriculaService.obtenerPorAlumno(5L)
        );

        verify(matriculaRepository, never()).findByAlumnoId(any());
    }

    @Test
    void obtenerPorAlumno_usuarioInexistente_deberiaLanzarResourceNotFoundException() {

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> matriculaService.obtenerPorAlumno(99L)
        );

        verify(usuarioActualService, never()).obtenerUsuarioActual();
    }

    @Test
    void obtenerPorAlumno_usuarioQueNoEsAlumno_deberiaLanzarIllegalArgumentException() {

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(profesor));

        assertThrows(
                IllegalArgumentException.class,
                () -> matriculaService.obtenerPorAlumno(2L)
        );

        verify(usuarioActualService, never()).obtenerUsuarioActual();
    }
}