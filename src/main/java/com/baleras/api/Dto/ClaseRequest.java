package com.baleras.api.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClaseRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @Size(max = 50, message = "El grupo no puede superar los 50 caracteres")
        String grupo,

        @Size(max = 50, message = "El aula no puede superar los 50 caracteres")
        String aula,

        @NotNull(message = "La asignatura es obligatoria")
        Long asignaturaId,

        @NotNull(message = "El profesor es obligatorio")
        Long profesorId

) {}