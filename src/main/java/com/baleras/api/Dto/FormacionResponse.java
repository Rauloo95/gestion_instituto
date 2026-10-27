package com.baleras.api.Dto;

public record FormacionResponse(
        Long id,
        String nombre,
        String descripcion,
        String nivel,
        String codigo,
        Long centroId,
        String centroNombre
) {
}
