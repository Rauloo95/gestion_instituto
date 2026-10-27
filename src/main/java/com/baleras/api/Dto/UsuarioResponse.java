package com.baleras.api.Dto;

import com.baleras.api.Entity.Rol;

public record UsuarioResponse(

        //l oque devuelve al usuario
        Long id,
        String nombre,
        String apellidos,
        String email,
        Rol rol
) {
}
