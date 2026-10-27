package com.baleras.api.Dto;

import com.baleras.api.Entity.EstadoMatricula;

import java.time.LocalDate;

public record MatriculaResponse(

        Long id,

        Long alumnoId,
        String alumnoNombre,

        Long claseId,
        String claseNombre,

        Long asignaturaId,
        String asignaturaNombre,

        LocalDate fechaMatricula,

        EstadoMatricula estado

) {}
