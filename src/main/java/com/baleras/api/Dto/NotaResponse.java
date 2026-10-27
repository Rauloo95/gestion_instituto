package com.baleras.api.Dto;

import java.time.LocalDateTime;

public record NotaResponse(

        Long id,

        Double valor,

        String observaciones,

        LocalDateTime fechaEvaluacion,

        Long matriculaId,

        Long alumnoId,
        String alumnoNombre,

        Long claseId,
        String claseNombre,

        Long asignaturaId,
        String asignaturaNombre,

        Long profesorId,
        String profesorNombre

) {}
