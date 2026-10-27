package com.baleras.api.Service;

import com.baleras.api.Dto.AsignaturaRequest;
import com.baleras.api.Dto.AsignaturaResponse;
import com.baleras.api.Entity.Asignatura;
import com.baleras.api.Entity.Formacion;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.AsignaturaRepository;
import com.baleras.api.Repository.FormacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;
    private final FormacionRepository formacionRepository;

    public AsignaturaService(
            AsignaturaRepository asignaturaRepository,
            FormacionRepository formacionRepository) {

        this.asignaturaRepository = asignaturaRepository;
        this.formacionRepository = formacionRepository;
    }

    public List<AsignaturaResponse> obtenerTodos() {

        return asignaturaRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public AsignaturaResponse obtenerPorId(Long id) {

        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la asignatura con ID: " + id
                        )
                );

        return convertirAResponse(asignatura);
    }

    public List<AsignaturaResponse> obtenerPorFormacion(
            Long formacionId) {

        if (!formacionRepository.existsById(formacionId)) {
            throw new ResourceNotFoundException(
                    "No se ha encontrado la formación con ID: "
                            + formacionId
            );
        }

        return asignaturaRepository
                .findByFormacionId(formacionId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public AsignaturaResponse crear(AsignaturaRequest request) {

        Formacion formacion = formacionRepository
                .findById(request.formacionId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la formación con ID: "
                                        + request.formacionId()
                        )
                );

        if (asignaturaRepository.existsByNombreAndFormacionId(
                request.nombre(),
                request.formacionId())) {

            throw new DuplicateResourceException(
                    "Ya existe una asignatura con ese nombre en esta formación"
            );
        }

        Asignatura asignatura = Asignatura.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .codigo(request.codigo())
                .curso(request.curso())
                .formacion(formacion)
                .build();

        Asignatura asignaturaGuardada =
                asignaturaRepository.save(asignatura);

        return convertirAResponse(asignaturaGuardada);
    }

    public AsignaturaResponse actualizar(
            Long id,
            AsignaturaRequest request) {

        Asignatura asignatura = asignaturaRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la asignatura con ID: "
                                        + id
                        )
                );

        Formacion formacion = formacionRepository
                .findById(request.formacionId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la formación con ID: "
                                        + request.formacionId()
                        )
                );

        if ((!asignatura.getNombre().equals(request.nombre())
                || !asignatura.getFormacion()
                .getId()
                .equals(request.formacionId()))
                && asignaturaRepository
                .existsByNombreAndFormacionId(
                        request.nombre(),
                        request.formacionId())) {

            throw new DuplicateResourceException(
                    "Ya existe una asignatura con ese nombre en esta formación"
            );
        }

        asignatura.setNombre(request.nombre());
        asignatura.setDescripcion(request.descripcion());
        asignatura.setCodigo(request.codigo());
        asignatura.setCurso(request.curso());
        asignatura.setFormacion(formacion);

        Asignatura asignaturaActualizada =
                asignaturaRepository.save(asignatura);

        return convertirAResponse(asignaturaActualizada);
    }

    public void eliminar(Long id) {

        Asignatura asignatura = asignaturaRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la asignatura con ID: "
                                        + id
                        )
                );

        asignaturaRepository.delete(asignatura);
    }

    private AsignaturaResponse convertirAResponse(
            Asignatura asignatura) {

        return new AsignaturaResponse(
                asignatura.getId(),
                asignatura.getNombre(),
                asignatura.getDescripcion(),
                asignatura.getCodigo(),
                asignatura.getCurso(),
                asignatura.getFormacion().getId(),
                asignatura.getFormacion().getNombre()
        );
    }
}
