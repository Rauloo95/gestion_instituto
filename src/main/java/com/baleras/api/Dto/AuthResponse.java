package com.baleras.api.Dto;

import com.baleras.api.Entity.Rol;

public record AuthResponse(
        Long id,
        String nombre,
        String email,
        Rol rol,
        String token
) {
}
