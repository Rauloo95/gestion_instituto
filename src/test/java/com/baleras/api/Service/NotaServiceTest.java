package com.baleras.api.Service;

import com.baleras.api.Dto.NotaRequest;
import com.baleras.api.Dto.NotaResponse;
import com.baleras.api.Entity.*;
import com.baleras.api.Exception.AccessDeniedException;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.MatriculaRepository;
import com.baleras.api.Repository.NotaRepository;
import com.baleras.api.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotaServiceTest {

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioActualService usuarioActualService;

    @InjectMocks
    private NotaService notaService;

    private Usuario admin;
    private Usuario profesor;
    private Usuario otroProfesor;
    private Usuario alumno;
    private Usuario otroAlumno;

    private Asignatura asignatura;
    private Clase clase;
    private Matricula matricula;
    private Nota nota;

    @BeforeEach
    void setUp() {

        admin = crearUsuario(1L, "Admin", Rol.ADMIN);
        profesor = crearUsuario(2L, "Profesor", Rol.PROFESOR);
        otroProfesor = crearUsuario(3L, "Otro profesor", Rol.PROFESOR);
        alumno = crearUsuario(4L, "Alumno", Rol.ALUMNO);
        otroAlumno = crearUsuario(5L, "Otro alumno", Rol.ALUMNO);

        asignatura = new Asignatura();
        asignatura.setId(1L);
        asignatura.setNombre("Programación");

        clase = new Clase();
        clase.setId(1L);
        clase.setNombre("Grupo A");
        clase.setProfesor(profesor);
        clase.setAsignatura(asignatura);

        matricula = new Matricula();
        matricula.setId(1L);
        matricula.setAlumno(alumno);
        matricula.setClase(clase);

        nota = new Nota();
        nota.setId(1L);
        nota.setValor(8.5);
        nota.setObservaciones("Buen trabajo");
        nota.setFechaEvaluacion(LocalDateTime.now());
        nota.setMatricula(matricula);
        nota.setProfesor(profesor);
    }

    private Usuario crearUsuario(Long id, String nombre, Rol rol) {

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setRol(rol);

        return usuario;
    }

    private Nota crearNota(Long id, Double valor, Matricula matricula, Usuario profesor) {

        Nota nuevaNota = new Nota();
        nuevaNota.setId(id);
        nuevaNota.setValor(valor);
        nuevaNota.setObservaciones("Observaciones");
        nuevaNota.setFechaEvaluacion(LocalDateTime.now());
        nuevaNota.setMatricula(matricula);
        nuevaNota.setProfesor(profesor);

        return nuevaNota;
    }

    // ----------------------------------------------------
    // OBTENER TODOS
    // ----------------------------------------------------

    @Test
    void obtenerTodos_devuelveListaDeNotas() {

        when(notaRepository.findAll()).thenReturn(List.of(nota));

        List<NotaResponse> resultado = notaService.obtenerTodos();

        assertEquals(1, resultado.size());
        assertEquals(8.5, resultado.get(0).valor());
        assertEquals("Alumno", resultado.get(0).alumnoNombre());
        assertEquals("Grupo A", resultado.get(0).claseNombre());
        assertEquals("Programación", resultado.get(0).asignaturaNombre());
        assertEquals("Profesor", resultado.get(0).profesorNombre());

        verify(notaRepository).findAll();
    }

    @Test
    void obtenerTodos_sinNotas_devuelveListaVacia() {

        when(notaRepository.findAll()).thenReturn(List.of());

        List<NotaResponse> resultado = notaService.obtenerTodos();

        assertTrue(resultado.isEmpty());
    }

    // ----------------------------------------------------
    // OBTENER POR ID
    // ----------------------------------------------------

    @Test
    void obtenerPorId_notaExistente_devuelveNota() {

        when(notaRepository.findById(1L)).thenReturn(Optional.of(nota));

        NotaResponse resultado = notaService.obtenerPorId(1L);

        assertEquals(1L, resultado.id());
        assertEquals(8.5, resultado.valor());
        assertEquals("Alumno", resultado.alumnoNombre());
    }

    @Test
    void obtenerPorId_notaInexistente_lanzaExcepcion() {

        when(notaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notaService.obtenerPorId(99L)
        );
    }

    // ----------------------------------------------------
    // OBTENER POR MATRÍCULA
    // ----------------------------------------------------

    @Test
    void obtenerPorMatricula_notaExistente_devuelveNota() {

        when(notaRepository.findByMatriculaId(1L))
                .thenReturn(Optional.of(nota));

        NotaResponse resultado = notaService.obtenerPorMatricula(1L);

        assertEquals(1L, resultado.matriculaId());
        assertEquals(8.5, resultado.valor());
    }

    @Test
    void obtenerPorMatricula_sinNota_lanzaExcepcion() {

        when(notaRepository.findByMatriculaId(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notaService.obtenerPorMatricula(99L)
        );
    }

    // ----------------------------------------------------
    // OBTENER POR ALUMNO
    // ----------------------------------------------------

    @Test
    void obtenerPorAlumno_alumnoExistente_devuelveNotas() {

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(notaRepository.findByMatriculaAlumnoId(4L))
                .thenReturn(List.of(nota));

        List<NotaResponse> resultado = notaService.obtenerPorAlumno(4L);

        assertEquals(1, resultado.size());
        assertEquals(8.5, resultado.get(0).valor());
    }

    @Test
    void obtenerPorAlumno_alumnoInexistente_lanzaExcepcion() {

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notaService.obtenerPorAlumno(99L)
        );
    }

    @Test
    void obtenerPorAlumno_usuarioNoEsAlumno_lanzaExcepcion() {

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(profesor));

        assertThrows(
                IllegalArgumentException.class,
                () -> notaService.obtenerPorAlumno(2L)
        );
    }

    @Test
    void obtenerPorAlumno_sinNotas_devuelveListaVacia() {

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        when(notaRepository.findByMatriculaAlumnoId(4L))
                .thenReturn(List.of());

        List<NotaResponse> resultado = notaService.obtenerPorAlumno(4L);

        assertTrue(resultado.isEmpty());
    }

    // ----------------------------------------------------
    // OBTENER POR PROFESOR
    // ----------------------------------------------------

    @Test
    void obtenerPorProfesor_profesorExistente_devuelveNotas() {

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(profesor));

        when(notaRepository.findByProfesorId(2L))
                .thenReturn(List.of(nota));

        List<NotaResponse> resultado = notaService.obtenerPorProfesor(2L);

        assertEquals(1, resultado.size());
        assertEquals("Profesor", resultado.get(0).profesorNombre());
    }

    @Test
    void obtenerPorProfesor_profesorInexistente_lanzaExcepcion() {

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notaService.obtenerPorProfesor(99L)
        );
    }

    @Test
    void obtenerPorProfesor_usuarioNoEsProfesor_lanzaExcepcion() {

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(alumno));

        assertThrows(
                IllegalArgumentException.class,
                () -> notaService.obtenerPorProfesor(4L)
        );
    }

    @Test
    void obtenerPorProfesor_sinNotas_devuelveListaVacia() {

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(profesor));

        when(notaRepository.findByProfesorId(2L))
                .thenReturn(List.of());

        List<NotaResponse> resultado = notaService.obtenerPorProfesor(2L);

        assertTrue(resultado.isEmpty());
    }

    // ----------------------------------------------------
    // CREAR
    // ----------------------------------------------------

    @Test
    void crear_datosCorrectos_creaNota() {

        NotaRequest request = new NotaRequest(
                9.0,
                "Excelente",
                1L
        );

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        when(notaRepository.findByMatriculaId(1L))
                .thenReturn(Optional.empty());

        when(notaRepository.save(any(Nota.class)))
                .thenAnswer(invocation -> {
                    Nota guardada = invocation.getArgument(0);
                    guardada.setId(1L);
                    return guardada;
                });

        NotaResponse resultado = notaService.crear(request);

        assertEquals(9.0, resultado.valor());
        assertEquals("Excelente", resultado.observaciones());
        assertEquals(1L, resultado.matriculaId());
        assertEquals(2L, resultado.profesorId());

        verify(notaRepository).save(any(Nota.class));
    }

    @Test
    void crear_matriculaInexistente_lanzaExcepcion() {

        NotaRequest request = new NotaRequest(8.0, null, 99L);

        when(matriculaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notaService.crear(request)
        );
    }

    @Test
    void crear_usuarioAdmin_lanzaExcepcion() {

        NotaRequest request = new NotaRequest(8.0, null, 1L);

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(admin);

        assertThrows(
                AccessDeniedException.class,
                () -> notaService.crear(request)
        );
    }

    @Test
    void crear_usuarioAlumno_lanzaExcepcion() {

        NotaRequest request = new NotaRequest(8.0, null, 1L);

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(alumno);

        assertThrows(
                AccessDeniedException.class,
                () -> notaService.crear(request)
        );
    }

    @Test
    void crear_profesorDeOtraClase_lanzaExcepcion() {

        NotaRequest request = new NotaRequest(8.0, null, 1L);

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(otroProfesor);

        assertThrows(
                AccessDeniedException.class,
                () -> notaService.crear(request)
        );
    }

    @Test
    void crear_matriculaYaTieneNota_lanzaExcepcion() {

        NotaRequest request = new NotaRequest(8.0, null, 1L);

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        when(notaRepository.findByMatriculaId(1L))
                .thenReturn(Optional.of(nota));

        assertThrows(
                DuplicateResourceException.class,
                () -> notaService.crear(request)
        );
    }

    // ----------------------------------------------------
    // ACTUALIZAR
    // ----------------------------------------------------

    @Test
    void actualizar_datosCorrectos_actualizaNota() {

        NotaRequest request = new NotaRequest(
                9.5,
                "Actualizada",
                1L
        );

        when(notaRepository.findById(1L))
                .thenReturn(Optional.of(nota));

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        when(notaRepository.save(any(Nota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotaResponse resultado = notaService.actualizar(1L, request);

        assertEquals(9.5, resultado.valor());
        assertEquals("Actualizada", resultado.observaciones());

        verify(notaRepository).save(nota);
    }

    @Test
    void actualizar_notaInexistente_lanzaExcepcion() {

        NotaRequest request = new NotaRequest(8.0, null, 1L);

        when(notaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notaService.actualizar(99L, request)
        );
    }

    @Test
    void actualizar_matriculaInexistente_lanzaExcepcion() {

        NotaRequest request = new NotaRequest(8.0, null, 99L);

        when(notaRepository.findById(1L))
                .thenReturn(Optional.of(nota));

        when(matriculaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notaService.actualizar(1L, request)
        );
    }

    @Test
    void actualizar_usuarioAdmin_lanzaExcepcion() {

        NotaRequest request = new NotaRequest(8.0, null, 1L);

        when(notaRepository.findById(1L))
                .thenReturn(Optional.of(nota));

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(admin);

        assertThrows(
                AccessDeniedException.class,
                () -> notaService.actualizar(1L, request)
        );
    }

    @Test
    void actualizar_profesorDeOtraClase_lanzaExcepcion() {

        NotaRequest request = new NotaRequest(8.0, null, 1L);

        when(notaRepository.findById(1L))
                .thenReturn(Optional.of(nota));

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(otroProfesor);

        assertThrows(
                AccessDeniedException.class,
                () -> notaService.actualizar(1L, request)
        );
    }

    @Test
    void actualizar_cambiaMatriculaYYaExisteOtraNota_lanzaExcepcion() {

        Matricula otraMatricula = new Matricula();
        otraMatricula.setId(2L);
        otraMatricula.setAlumno(otroAlumno);
        otraMatricula.setClase(clase);

        NotaRequest request = new NotaRequest(8.0, null, 2L);

        when(notaRepository.findById(1L))
                .thenReturn(Optional.of(nota));

        when(matriculaRepository.findById(2L))
                .thenReturn(Optional.of(otraMatricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        when(notaRepository.findByMatriculaId(2L))
                .thenReturn(Optional.of(nota));

        assertThrows(
                DuplicateResourceException.class,
                () -> notaService.actualizar(1L, request)
        );
    }

    @Test
    void actualizar_mismaMatricula_noCompruebaDuplicados() {

        NotaRequest request = new NotaRequest(7.5, "Modificada", 1L);

        when(notaRepository.findById(1L))
                .thenReturn(Optional.of(nota));

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        when(notaRepository.save(any(Nota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotaResponse resultado = notaService.actualizar(1L, request);

        assertEquals(7.5, resultado.valor());

        verify(notaRepository, never()).findByMatriculaId(anyLong());
    }

    // ----------------------------------------------------
    // ELIMINAR
    // ----------------------------------------------------

    @Test
    void eliminar_profesorPropietario_eliminaNota() {

        when(notaRepository.findById(1L))
                .thenReturn(Optional.of(nota));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        assertDoesNotThrow(() -> notaService.eliminar(1L));

        verify(notaRepository).delete(nota);
    }

    @Test
    void eliminar_notaInexistente_lanzaExcepcion() {

        when(notaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notaService.eliminar(99L)
        );
    }

    @Test
    void eliminar_usuarioAdmin_lanzaExcepcion() {

        when(notaRepository.findById(1L))
                .thenReturn(Optional.of(nota));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(admin);

        assertThrows(
                AccessDeniedException.class,
                () -> notaService.eliminar(1L)
        );
    }

    @Test
    void eliminar_profesorDeOtraClase_lanzaExcepcion() {

        when(notaRepository.findById(1L))
                .thenReturn(Optional.of(nota));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(otroProfesor);

        assertThrows(
                AccessDeniedException.class,
                () -> notaService.eliminar(1L)
        );
    }

    // ----------------------------------------------------
    // OBTENER MIS NOTAS
    // ----------------------------------------------------

    @Test
    void obtenerMisNotas_usuarioAlumno_devuelveSusNotas() {

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(alumno);

        when(notaRepository.findByMatriculaAlumnoId(4L))
                .thenReturn(List.of(nota));

        List<NotaResponse> resultado = notaService.obtenerMisNotas();

        assertEquals(1, resultado.size());
        assertEquals(8.5, resultado.get(0).valor());

        verify(notaRepository).findByMatriculaAlumnoId(4L);
    }

    @Test
    void obtenerMisNotas_usuarioNoEsAlumno_lanzaExcepcion() {

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        assertThrows(
                AccessDeniedException.class,
                () -> notaService.obtenerMisNotas()
        );

        verify(notaRepository, never()).findByMatriculaAlumnoId(anyLong());
    }

    @Test
    void obtenerMisNotas_sinNotas_devuelveListaVacia() {

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(alumno);

        when(notaRepository.findByMatriculaAlumnoId(4L))
                .thenReturn(List.of());

        List<NotaResponse> resultado = notaService.obtenerMisNotas();

        assertTrue(resultado.isEmpty());
    }


    @Test
    void actualizar_notaDeOtroProfesor_lanzaExcepcion() {

        // Creamos una clase perteneciente a otro profesor
        Clase claseAjena = new Clase();
        claseAjena.setId(2L);
        claseAjena.setNombre("Grupo B");
        claseAjena.setProfesor(otroProfesor);
        claseAjena.setAsignatura(asignatura);

        // La nota original pertenece a otro profesor
        Matricula matriculaAjena = new Matricula();
        matriculaAjena.setId(2L);
        matriculaAjena.setAlumno(otroAlumno);
        matriculaAjena.setClase(claseAjena);

        Nota notaAjena = crearNota(
                2L,
                7.0,
                matriculaAjena,
                otroProfesor
        );

        // El profesor intenta modificarla utilizando su propia matrícula
        NotaRequest request = new NotaRequest(
                9.0,
                "Intento de modificación",
                1L
        );

        when(notaRepository.findById(2L))
                .thenReturn(Optional.of(notaAjena));

        when(matriculaRepository.findById(1L))
                .thenReturn(Optional.of(matricula));

        when(usuarioActualService.obtenerUsuarioActual())
                .thenReturn(profesor);

        assertThrows(
                AccessDeniedException.class,
                () -> notaService.actualizar(2L, request)
        );

        verify(notaRepository, never()).save(any(Nota.class));
    }
}


