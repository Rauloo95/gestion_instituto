package com.baleras.api.Service;
import com.baleras.api.Dto.FormacionRequest;
import com.baleras.api.Dto.FormacionResponse;
import com.baleras.api.Entity.Centro;
import com.baleras.api.Entity.Formacion;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.CentroRepository;
import com.baleras.api.Repository.FormacionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class FormacionService {
    private final FormacionRepository formacionRepository;
    private final CentroRepository centroRepository;

    public FormacionService(
            FormacionRepository formacionRepository,
            CentroRepository centroRepository) {

        this.formacionRepository = formacionRepository;
        this.centroRepository = centroRepository;
    }

    public List<FormacionResponse> obtenerTodos() {

        return formacionRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public FormacionResponse obtenerPorId(Long id) {

        Formacion formacion = formacionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la formación con ID: " + id
                        )
                );

        return convertirAResponse(formacion);
    }

    public List<FormacionResponse> obtenerPorCentro(Long centroId) {

        if (!centroRepository.existsById(centroId)) {
            throw new ResourceNotFoundException(
                    "No se ha encontrado el centro con ID: " + centroId
            );
        }

        return formacionRepository.findByCentroId(centroId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public FormacionResponse crear(FormacionRequest request) {

        Centro centro = centroRepository.findById(request.centroId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el centro con ID: "
                                        + request.centroId()
                        )
                );

        if (formacionRepository.existsByNombreAndCentroId(
                request.nombre(),
                request.centroId())) {

            throw new DuplicateResourceException(
                    "Ya existe una formación con ese nombre en este centro"
            );
        }

        Formacion formacion = Formacion.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .nivel(request.nivel())
                .codigo(request.codigo())
                .centro(centro)
                .build();

        Formacion formacionGuardada =
                formacionRepository.save(formacion);

        return convertirAResponse(formacionGuardada);
    }

    public FormacionResponse actualizar(
            Long id,
            FormacionRequest request) {

        Formacion formacion = formacionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la formación con ID: "
                                        + id
                        )
                );

        Centro centro = centroRepository.findById(request.centroId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el centro con ID: "
                                        + request.centroId()
                        )
                );

        if ((!formacion.getNombre().equals(request.nombre())
                || !formacion.getCentro().getId().equals(request.centroId()))
                && formacionRepository.existsByNombreAndCentroId(
                request.nombre(),
                request.centroId())) {

            throw new DuplicateResourceException(
                    "Ya existe una formación con ese nombre en este centro"
            );
        }

        formacion.setNombre(request.nombre());
        formacion.setDescripcion(request.descripcion());
        formacion.setNivel(request.nivel());
        formacion.setCodigo(request.codigo());
        formacion.setCentro(centro);

        Formacion formacionActualizada =
                formacionRepository.save(formacion);

        return convertirAResponse(formacionActualizada);
    }

    public void eliminar(Long id) {

        Formacion formacion = formacionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado la formación con ID: "
                                        + id
                        )
                );

        formacionRepository.delete(formacion);
    }

    private FormacionResponse convertirAResponse(
            Formacion formacion) {

        return new FormacionResponse(
                formacion.getId(),
                formacion.getNombre(),
                formacion.getDescripcion(),
                formacion.getNivel(),
                formacion.getCodigo(),
                formacion.getCentro().getId(),
                formacion.getCentro().getNombre()
        );
    }


}
