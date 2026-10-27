package com.baleras.api.Dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record FormacionRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @Size(max = 50, message = "El nivel no puede superar los 50 caracteres")
        String nivel,

        @Size(max = 20, message = "El código no puede superar los 20 caracteres")
        String codigo,

        @NotNull(message = "El centro es obligatorio")
        Long centroId
) {
}
