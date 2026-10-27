package com.baleras.api.Dto;

public record CentroResponse(
        Long id,
        String nombre,
        String tipo,
        String direccion,
        String ciudad,
        String codigoPostal,
        String telefono,
        String email,
        Boolean activo
) {
}
