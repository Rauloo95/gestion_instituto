package com.baleras.api.Controller;

import com.baleras.api.Dto.FormacionRequest;
import com.baleras.api.Dto.FormacionResponse;
import com.baleras.api.Service.FormacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formaciones")
public class FormacionController {

    private final FormacionService formacionService;

    public FormacionController(FormacionService formacionService) {
        this.formacionService = formacionService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping
    public ResponseEntity<List<FormacionResponse>> obtenerTodos() {

        return ResponseEntity.ok(
                formacionService.obtenerTodos()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/centro/{centroId}")
    public ResponseEntity<List<FormacionResponse>> obtenerPorCentro(
            @PathVariable Long centroId) {

        return ResponseEntity.ok(
                formacionService.obtenerPorCentro(centroId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/{id}")
    public ResponseEntity<FormacionResponse> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                formacionService.obtenerPorId(id)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FormacionResponse> crear(
            @Valid @RequestBody FormacionRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(formacionService.crear(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FormacionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody FormacionRequest request) {

        return ResponseEntity.ok(
                formacionService.actualizar(id, request)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        formacionService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}