package com.baleras.api.Dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotaRequest(

        @NotNull(message = "La nota es obligatoria")
        @DecimalMin(value = "0.0", message = "La nota no puede ser inferior a 0")
        @DecimalMax(value = "10.0", message = "La nota no puede ser superior a 10")
        Double valor,

        @Size(
                max = 500,
                message = "Las observaciones no pueden superar los 500 caracteres"
        )
        String observaciones,

        @NotNull(message = "La matrícula es obligatoria")
        Long matriculaId
) {}
