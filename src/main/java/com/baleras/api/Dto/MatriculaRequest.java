package com.baleras.api.Dto;

import com.baleras.api.Entity.EstadoMatricula;
import jakarta.validation.constraints.NotNull;

public record MatriculaRequest(

        @NotNull(message = "El alumno es obligatorio")
        Long alumnoId,

        @NotNull(message = "La clase es obligatoria")
        Long claseId,

        EstadoMatricula estado

) {}
