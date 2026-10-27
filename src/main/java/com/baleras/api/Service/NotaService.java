package com.baleras.api.Service;

import com.baleras.api.Dto.NotaRequest;
import com.baleras.api.Dto.NotaResponse;
import com.baleras.api.Entity.Matricula;
import com.baleras.api.Entity.Nota;
import com.baleras.api.Entity.Rol;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Exception.AccessDeniedException;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.MatriculaRepository;
import com.baleras.api.Repository.NotaRepository;
import com.baleras.api.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotaService {

    private final NotaRepository notaRepository;
    private final MatriculaRepository matriculaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActualService usuarioActualService;

    public NotaService(
            NotaRepository notaRepository,
            MatriculaRepository matriculaRepository,
            UsuarioRepository usuarioRepository, UsuarioActualService usuarioActualService) {

        this.notaRepository = notaRepository;
        this.matriculaRepository = matriculaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioActualService = usuarioActualService;
    }

    public List<NotaResponse> obtenerTodos() {

        return notaRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public NotaResponse obtenerPorId(Long id) {

        Nota nota = notaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la nota con ID: "
                                        + id
                        )
                );

        return convertirAResponse(nota);
    }

    public NotaResponse obtenerPorMatricula(Long matriculaId) {

        Nota nota = notaRepository
                .findByMatriculaId(matriculaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado una nota para la matrícula con ID: "
                                        + matriculaId
                        )
                );

        return convertirAResponse(nota);
    }

    public List<NotaResponse> obtenerPorAlumno(Long alumnoId) {

        validarAlumno(alumnoId);

        return notaRepository.findByMatriculaAlumnoId(alumnoId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public List<NotaResponse> obtenerPorProfesor(Long profesorId) {

        validarProfesor(profesorId);

        return notaRepository.findByProfesorId(profesorId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public NotaResponse crear(NotaRequest request) {

        Matricula matricula = matriculaRepository.findById(request.matriculaId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la matrícula con ID: "
                                        + request.matriculaId()
                        )
                );

        Usuario usuarioActual =
                usuarioActualService.obtenerUsuarioActual();

        Usuario profesor;

        // ADMIN puede crear una nota, pero necesita ser PROFESOR
        if (usuarioActual.getRol() == Rol.ADMIN) {

            throw new AccessDeniedException(
                    "Un administrador no puede crear una nota directamente"
            );
        }

        // El profesor autenticado es el profesor de la nota
        if (usuarioActual.getRol() == Rol.PROFESOR) {

            profesor = usuarioActual;

            Long profesorDeLaClase =
                    matricula.getClase()
                            .getProfesor()
                            .getId();

            if (!profesor.getId().equals(profesorDeLaClase)) {

                throw new AccessDeniedException(
                        "No puedes crear una nota para una clase que no te pertenece"
                );
            }

        } else {

            throw new AccessDeniedException(
                    "No tienes permiso para crear notas"
            );
        }

        if (notaRepository.findByMatriculaId(request.matriculaId()).isPresent()) {

            throw new DuplicateResourceException(
                    "Esta matrícula ya tiene una nota"
            );
        }

        Nota nota = Nota.builder()
                .valor(request.valor())
                .observaciones(request.observaciones())
                .fechaEvaluacion(LocalDateTime.now())
                .matricula(matricula)
                .profesor(profesor)
                .build();

        return convertirAResponse(
                notaRepository.save(nota)
        );
    }

    public NotaResponse actualizar(Long id, NotaRequest request) {

        Nota nota = notaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la nota con ID: " + id
                        )
                );

        Matricula matricula = matriculaRepository.findById(request.matriculaId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la matrícula con ID: "
                                        + request.matriculaId()
                        )
                );

        Usuario usuarioActual =
                usuarioActualService.obtenerUsuarioActual();

        if (usuarioActual.getRol() != Rol.PROFESOR) {

            throw new AccessDeniedException(
                    "Solo un profesor puede modificar notas"
            );
        }

        Long profesorDeLaClase =
                matricula.getClase()
                        .getProfesor()
                        .getId();

        if (!usuarioActual.getId().equals(profesorDeLaClase)) {

            throw new AccessDeniedException(
                    "No puedes modificar una nota de una clase que no te pertenece"
            );
        }

        if (!nota.getMatricula().getId().equals(request.matriculaId())) {

            if (notaRepository.findByMatriculaId(request.matriculaId()).isPresent()) {

                throw new DuplicateResourceException(
                        "Esta matrícula ya tiene una nota"
                );
            }
        }

        nota.setValor(request.valor());
        nota.setObservaciones(request.observaciones());
        nota.setMatricula(matricula);
        nota.setProfesor(usuarioActual);

        return convertirAResponse(
                notaRepository.save(nota)
        );
    }

    public void eliminar(Long id) {

        Nota nota = notaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la nota con ID: " + id
                        )
                );

        Usuario usuarioActual =
                usuarioActualService.obtenerUsuarioActual();

        if (usuarioActual.getRol() != Rol.PROFESOR) {

            throw new AccessDeniedException(
                    "Solo un profesor puede eliminar notas"
            );
        }

        Long profesorDeLaClase =
                nota.getMatricula()
                        .getClase()
                        .getProfesor()
                        .getId();

        if (!usuarioActual.getId().equals(profesorDeLaClase)) {

            throw new AccessDeniedException(
                    "No puedes eliminar una nota de una clase que no te pertenece"
            );
        }

        notaRepository.delete(nota);
    }

    private Usuario validarProfesor(Long profesorId) {

        Usuario profesor = usuarioRepository.findById(profesorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el usuario con ID: "
                                        + profesorId
                        )
                );

        validarProfesor(profesor);

        return profesor;
    }

    private void validarProfesor(Usuario usuario) {

        if (usuario.getRol() != Rol.PROFESOR) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no tiene el rol de PROFESOR"
            );
        }
    }

    public List<NotaResponse> obtenerMisNotas() {

        Usuario usuarioActual =
                usuarioActualService.obtenerUsuarioActual();

        if (usuarioActual.getRol() != Rol.ALUMNO) {

            throw new AccessDeniedException(
                    "Este endpoint solo está disponible para alumnos"
            );
        }

        return notaRepository
                .findByMatriculaAlumnoId(usuarioActual.getId())
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    private void validarAlumno(Long alumnoId) {

        Usuario alumno = usuarioRepository.findById(alumnoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el usuario con ID: "
                                        + alumnoId
                        )
                );

        if (alumno.getRol() != Rol.ALUMNO) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no tiene el rol de ALUMNO"
            );
        }
    }

    private NotaResponse convertirAResponse(Nota nota) {

        return new NotaResponse(
                nota.getId(),
                nota.getValor(),
                nota.getObservaciones(),
                nota.getFechaEvaluacion(),

                nota.getMatricula().getId(),

                nota.getMatricula()
                        .getAlumno()
                        .getId(),

                nota.getMatricula()
                        .getAlumno()
                        .getNombre(),

                nota.getMatricula()
                        .getClase()
                        .getId(),

                nota.getMatricula()
                        .getClase()
                        .getNombre(),

                nota.getMatricula()
                        .getClase()
                        .getAsignatura()
                        .getId(),

                nota.getMatricula()
                        .getClase()
                        .getAsignatura()
                        .getNombre(),

                nota.getProfesor().getId(),

                nota.getProfesor().getNombre()
        );
    }
}
