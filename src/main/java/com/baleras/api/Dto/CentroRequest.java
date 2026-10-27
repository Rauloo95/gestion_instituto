package com.baleras.api.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CentroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El tipo de centro es obligatorio")
        @Size(max = 50, message = "El tipo no puede superar los 50 caracteres")
        String tipo,

        @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
        String direccion,

        @Size(max = 100, message = "La ciudad no puede superar los 100 caracteres")
        String ciudad,

        @Size(max = 10, message = "El código postal no puede superar los 10 caracteres")
        String codigoPostal,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String telefono,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150, message = "El email no puede superar los 150 caracteres")
        String email
) {
}
