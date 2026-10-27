package com.baleras.api.Dto;

public record ClaseResponse(

        Long id,
        String nombre,
        String grupo,
        String aula,
        Boolean activo,

        Long asignaturaId,
        String asignaturaNombre,

        Long profesorId,
        String profesorNombre

) {}