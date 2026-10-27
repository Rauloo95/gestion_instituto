package com.baleras.api.Service;

import com.baleras.api.Dto.CentroRequest;
import com.baleras.api.Dto.CentroResponse;
import com.baleras.api.Entity.Centro;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.CentroRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CentroService {
    private final CentroRepository centroRepository;

    public CentroService(CentroRepository centroRepository) {
        this.centroRepository = centroRepository;
    }

    public List<CentroResponse> obtenerTodos() {

        return centroRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public CentroResponse obtenerPorId(Long id) {

        Centro centro = centroRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el centro con ID: " + id
                        )
                );

        return convertirAResponse(centro);
    }

    public CentroResponse crear(CentroRequest request) {

        if (centroRepository.existsByNombre(request.nombre())) {
            throw new DuplicateResourceException(
                    "Ya existe un centro con el nombre: " + request.nombre()
            );
        }

        Centro centro = Centro.builder()
                .nombre(request.nombre())
                .tipo(request.tipo())
                .direccion(request.direccion())
                .ciudad(request.ciudad())
                .codigoPostal(request.codigoPostal())
                .telefono(request.telefono())
                .email(request.email())
                .activo(true)
                .build();

        Centro centroGuardado = centroRepository.save(centro);

        return convertirAResponse(centroGuardado);
    }

    public CentroResponse actualizar(Long id, CentroRequest request) {

        Centro centro = centroRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el centro con ID: " + id
                        )
                );

        if (!centro.getNombre().equals(request.nombre())
                && centroRepository.existsByNombre(request.nombre())) {

            throw new DuplicateResourceException(
                    "Ya existe un centro con el nombre: " + request.nombre()
            );
        }

        centro.setNombre(request.nombre());
        centro.setTipo(request.tipo());
        centro.setDireccion(request.direccion());
        centro.setCiudad(request.ciudad());
        centro.setCodigoPostal(request.codigoPostal());
        centro.setTelefono(request.telefono());
        centro.setEmail(request.email());

        Centro centroActualizado = centroRepository.save(centro);

        return convertirAResponse(centroActualizado);
    }

    public void eliminar(Long id) {

        Centro centro = centroRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el centro con ID: " + id
                        )
                );

        centroRepository.delete(centro);
    }

    private CentroResponse convertirAResponse(Centro centro) {

        return new CentroResponse(
                centro.getId(),
                centro.getNombre(),
                centro.getTipo(),
                centro.getDireccion(),
                centro.getCiudad(),
                centro.getCodigoPostal(),
                centro.getTelefono(),
                centro.getEmail(),
                centro.getActivo()
        );
    }
}
