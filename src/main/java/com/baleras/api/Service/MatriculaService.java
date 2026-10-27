package com.baleras.api.Service;

import com.baleras.api.Dto.MatriculaRequest;
import com.baleras.api.Dto.MatriculaResponse;
import com.baleras.api.Entity.Clase;
import com.baleras.api.Entity.EstadoMatricula;
import com.baleras.api.Entity.Matricula;
import com.baleras.api.Entity.Rol;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Exception.AccessDeniedException;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.ClaseRepository;
import com.baleras.api.Repository.MatriculaRepository;
import com.baleras.api.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MatriculaService {

        private final MatriculaRepository matriculaRepository;
        private final UsuarioRepository usuarioRepository;
        private final ClaseRepository claseRepository;
        private final UsuarioActualService usuarioActualService;

        public MatriculaService(
                        MatriculaRepository matriculaRepository,
                        UsuarioRepository usuarioRepository,
                        ClaseRepository claseRepository, UsuarioActualService usuarioActualService) {

                this.matriculaRepository = matriculaRepository;
                this.usuarioRepository = usuarioRepository;
                this.claseRepository = claseRepository;
                this.usuarioActualService = usuarioActualService;
        }

        public List<MatriculaResponse> obtenerTodos() {

                return matriculaRepository.findAll()
                                .stream()
                                .map(this::convertirAResponse)
                                .toList();
        }

        public MatriculaResponse obtenerPorId(Long id) {

                Matricula matricula = matriculaRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se ha encontrado la matrícula con ID: "
                                                                + id));

                return convertirAResponse(matricula);
        }

        public List<MatriculaResponse> obtenerPorClase(Long claseId) {

                Clase clase = claseRepository.findById(claseId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se ha encontrado la clase con ID: " + claseId));

                Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();

                // ADMIN puede consultar cualquier clase
                if (usuarioActual.getRol() == Rol.ADMIN) {

                        return matriculaRepository.findByClaseId(claseId)
                                        .stream()
                                        .map(this::convertirAResponse)
                                        .toList();
                }

                // PROFESOR únicamente puede consultar alumnos de sus clases
                if (usuarioActual.getRol() == Rol.PROFESOR) {

                        if (!clase.getProfesor().getId().equals(usuarioActual.getId())) {
                                throw new AccessDeniedException(
                                                "No tienes permiso para consultar las matrículas de esta clase");
                        }

                        return matriculaRepository.findByClaseId(claseId)
                                        .stream()
                                        .map(this::convertirAResponse)
                                        .toList();
                }

                throw new AccessDeniedException(
                                "No tienes permiso para consultar estas matrículas");
        }

        public MatriculaResponse crear(MatriculaRequest request) {

                Usuario alumno = obtenerAlumno(request.alumnoId());

                Clase clase = claseRepository.findById(request.claseId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se ha encontrado la clase con ID: "
                                                                + request.claseId()));

                if (!clase.getActivo()) {
                        throw new IllegalArgumentException(
                                        "No se puede matricular un alumno en una clase inactiva");
                }

                if (matriculaRepository.existsByAlumnoIdAndClaseId(
                                request.alumnoId(),
                                request.claseId())) {

                        throw new DuplicateResourceException(
                                        "El alumno ya está matriculado en esta clase");
                }

                Matricula matricula = Matricula.builder()
                                .alumno(alumno)
                                .clase(clase)
                                .fechaMatricula(LocalDate.now())
                                .estado(
                                                request.estado() != null
                                                                ? request.estado()
                                                                : EstadoMatricula.ACTIVA)
                                .build();

                Matricula matriculaGuardada = matriculaRepository.save(matricula);

                return convertirAResponse(matriculaGuardada);
        }

        public MatriculaResponse actualizar(
                        Long id,
                        MatriculaRequest request) {

                Matricula matricula = matriculaRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se ha encontrado la matrícula con ID: "
                                                                + id));

                Usuario alumno = obtenerAlumno(request.alumnoId());

                Clase clase = claseRepository.findById(request.claseId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se ha encontrado la clase con ID: "
                                                                + request.claseId()));

                boolean haCambiadoAlumno = !matricula.getAlumno()
                                .getId()
                                .equals(request.alumnoId());

                boolean haCambiadoClase = !matricula.getClase()
                                .getId()
                                .equals(request.claseId());

                if ((haCambiadoAlumno || haCambiadoClase)
                                && matriculaRepository.existsByAlumnoIdAndClaseId(
                                                request.alumnoId(),
                                                request.claseId())) {

                        throw new DuplicateResourceException(
                                        "El alumno ya está matriculado en esta clase");
                }

                if (!clase.getActivo()) {
                        throw new IllegalArgumentException(
                                        "No se puede asociar una matrícula a una clase inactiva");
                }

                matricula.setAlumno(alumno);
                matricula.setClase(clase);

                if (request.estado() != null) {
                        matricula.setEstado(request.estado());
                }

                Matricula matriculaActualizada = matriculaRepository.save(matricula);

                return convertirAResponse(matriculaActualizada);
        }

        public void eliminar(Long id) {

                Matricula matricula = matriculaRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se ha encontrado la matrícula con ID: "
                                                                + id));

                matriculaRepository.delete(matricula);
        }

        public List<MatriculaResponse> obtenerPorAlumno(Long alumnoId) {

                Usuario alumno = obtenerAlumno(alumnoId);

                Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();

                // ADMIN puede consultar cualquier alumno
                if (usuarioActual.getRol() == Rol.ADMIN) {

                        return matriculaRepository.findByAlumnoId(alumno.getId())
                                        .stream()
                                        .map(this::convertirAResponse)
                                        .toList();
                }

                // PROFESOR podrá consultar el alumno solamente
                // si tiene una clase en la que está matriculado
                if (usuarioActual.getRol() == Rol.PROFESOR) {

                        boolean tieneMatriculaEnClaseDelProfesor = matriculaRepository.findByAlumnoId(alumnoId)
                                        .stream()
                                        .anyMatch(matricula -> matricula.getClase()
                                                        .getProfesor()
                                                        .getId()
                                                        .equals(usuarioActual.getId()));

                        if (!tieneMatriculaEnClaseDelProfesor) {
                                throw new AccessDeniedException(
                                                "No tienes permiso para consultar las matrículas de este alumno");
                        }

                        return matriculaRepository.findByAlumnoId(alumno.getId())
                                        .stream()
                                        .map(this::convertirAResponse)
                                        .toList();
                }

                // El alumno solo podrá consultar sus propias matrículas
                if (usuarioActual.getRol() == Rol.ALUMNO) {

                        if (!usuarioActual.getId().equals(alumnoId)) {
                                throw new AccessDeniedException(
                                                "No tienes permiso para consultar las matrículas de otro alumno");
                        }

                        return matriculaRepository.findByAlumnoId(alumno.getId())
                                        .stream()
                                        .map(this::convertirAResponse)
                                        .toList();
                }

                throw new AccessDeniedException(
                                "No tienes permiso para consultar estas matrículas");
        }

        private Usuario obtenerAlumno(Long alumnoId) {
                Usuario alumno = usuarioRepository.findById(alumnoId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se ha encontrado el usuario con ID: " + alumnoId));

                if (alumno.getRol() != Rol.ALUMNO) {
                        throw new IllegalArgumentException(
                                        "El usuario seleccionado no tiene el rol de ALUMNO");
                }

                return alumno;
        }

        private MatriculaResponse convertirAResponse(
                        Matricula matricula) {

                return new MatriculaResponse(
                                matricula.getId(),

                                matricula.getAlumno().getId(),
                                matricula.getAlumno().getNombre(),

                                matricula.getClase().getId(),
                                matricula.getClase().getNombre(),

                                matricula.getClase()
                                                .getAsignatura()
                                                .getId(),

                                matricula.getClase()
                                                .getAsignatura()
                                                .getNombre(),

                                matricula.getFechaMatricula(),
                                matricula.getEstado());
        }
}
