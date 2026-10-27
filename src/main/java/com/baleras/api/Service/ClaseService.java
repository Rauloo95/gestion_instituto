package com.baleras.api.Service;

import com.baleras.api.Dto.ClaseRequest;
import com.baleras.api.Dto.ClaseResponse;
import com.baleras.api.Entity.Asignatura;
import com.baleras.api.Entity.Clase;
import com.baleras.api.Entity.Rol;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.AsignaturaRepository;
import com.baleras.api.Repository.ClaseRepository;
import com.baleras.api.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import com.baleras.api.Exception.AccessDeniedException;

import java.util.List;

@Service
public class ClaseService {

    private final ClaseRepository claseRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActualService usuarioActualService;

    public ClaseService(
            ClaseRepository claseRepository,
            AsignaturaRepository asignaturaRepository,
            UsuarioRepository usuarioRepository, UsuarioActualService usuarioActualService) {

        this.claseRepository = claseRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioActualService = usuarioActualService;
    }

    public List<ClaseResponse> obtenerTodos() {

        return claseRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public ClaseResponse obtenerPorId(Long id) {

        Clase clase = claseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la clase con ID: " + id
                        )
                );

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();

        // El ADMIN puede consultar cualquier clase
        if (usuarioActual.getRol() == Rol.ADMIN) {
            return convertirAResponse(clase);
        }

        // Un PROFESOR solo puede consultar sus propias clases
        if (usuarioActual.getRol() == Rol.PROFESOR
                && !clase.getProfesor().getId().equals(usuarioActual.getId())) {

            throw new AccessDeniedException(
                    "No tienes permiso para consultar esta clase"
            );
        }

        throw new AccessDeniedException(
                "No tienes permiso para consultar esta clase"
        );
    }

    public List<ClaseResponse> obtenerPorAsignatura(Long asignaturaId) {

        if (!asignaturaRepository.existsById(asignaturaId)) {
            throw new ResourceNotFoundException(
                    "No se ha encontrado la asignatura con ID: " + asignaturaId
            );
        }

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();

        List<Clase> clases = claseRepository.findByAsignaturaId(asignaturaId);

        // ADMIN puede consultar todas
        if (usuarioActual.getRol() == Rol.ADMIN) {
            return clases.stream()
                    .map(this::convertirAResponse)
                    .toList();
        }

        // PROFESOR → únicamente sus clases
        if (usuarioActual.getRol() == Rol.PROFESOR) {
            return clases.stream()
                    .filter(clase ->
                            clase.getProfesor().getId().equals(usuarioActual.getId())
                    )
                    .map(this::convertirAResponse)
                    .toList();
        }

        throw new AccessDeniedException(
                "No tienes permiso para consultar estas clases"
        );
    }

    public List<ClaseResponse> obtenerPorProfesor(Long profesorId) {

        Usuario profesor = usuarioRepository.findById(profesorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el usuario con ID: " + profesorId
                        )
                );

        validarProfesor(profesor);

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();

        if (usuarioActual.getRol() == Rol.PROFESOR
                && !usuarioActual.getId().equals(profesorId)) {

            throw new AccessDeniedException(
                    "No tienes permiso para consultar las clases de otro profesor"
            );
        }

        return claseRepository.findByProfesorId(profesorId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public ClaseResponse crear(ClaseRequest request) {

        Asignatura asignatura = asignaturaRepository
                .findById(request.asignaturaId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la asignatura con ID: "
                                        + request.asignaturaId()
                        )
                );

        Usuario profesor = usuarioRepository
                .findById(request.profesorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el usuario con ID: "
                                        + request.profesorId()
                        )
                );

        validarProfesor(profesor);

        if (claseRepository.existsByNombreAndAsignaturaId(
                request.nombre(),
                request.asignaturaId())) {

            throw new DuplicateResourceException(
                    "Ya existe una clase con ese nombre en esta asignatura"
            );
        }

        Clase clase = Clase.builder()
                .nombre(request.nombre())
                .grupo(request.grupo())
                .aula(request.aula())
                .asignatura(asignatura)
                .profesor(profesor)
                .activo(true)
                .build();

        Clase claseGuardada = claseRepository.save(clase);

        return convertirAResponse(claseGuardada);
    }

    public ClaseResponse actualizar(
            Long id,
            ClaseRequest request) {

        Clase clase = claseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la clase con ID: "
                                        + id
                        )
                );

        Asignatura asignatura = asignaturaRepository
                .findById(request.asignaturaId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la asignatura con ID: "
                                        + request.asignaturaId()
                        )
                );

        Usuario profesor = usuarioRepository
                .findById(request.profesorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el usuario con ID: "
                                        + request.profesorId()
                        )
                );

        validarProfesor(profesor);

        if ((!clase.getNombre().equals(request.nombre())
                || !clase.getAsignatura()
                .getId()
                .equals(request.asignaturaId()))
                && claseRepository.existsByNombreAndAsignaturaId(
                request.nombre(),
                request.asignaturaId())) {

            throw new DuplicateResourceException(
                    "Ya existe una clase con ese nombre en esta asignatura"
            );
        }

        clase.setNombre(request.nombre());
        clase.setGrupo(request.grupo());
        clase.setAula(request.aula());
        clase.setAsignatura(asignatura);
        clase.setProfesor(profesor);

        Clase claseActualizada = claseRepository.save(clase);

        return convertirAResponse(claseActualizada);
    }

    public void eliminar(Long id) {

        Clase clase = claseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la clase con ID: "
                                        + id
                        )
                );

        claseRepository.delete(clase);
    }

    private void validarProfesor(Usuario usuario) {

        if (usuario.getRol() != Rol.PROFESOR) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no tiene el rol de PROFESOR"
            );
        }
    }

    private ClaseResponse convertirAResponse(Clase clase) {

        return new ClaseResponse(
                clase.getId(),
                clase.getNombre(),
                clase.getGrupo(),
                clase.getAula(),
                clase.getActivo(),
                clase.getAsignatura().getId(),
                clase.getAsignatura().getNombre(),
                clase.getProfesor().getId(),
                clase.getProfesor().getNombre()
        );
    }
}
