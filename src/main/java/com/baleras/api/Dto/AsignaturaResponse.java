package com.baleras.api.Dto;

public record AsignaturaResponse(
        Long id,
        String nombre,
        String descripcion,
        String codigo,
        String curso,
        Long formacionId,
        String formacionNombre
) {
}
