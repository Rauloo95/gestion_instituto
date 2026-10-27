package com.baleras.api.Controller;

import com.baleras.api.Dto.AsignaturaRequest;
import com.baleras.api.Dto.AsignaturaResponse;
import com.baleras.api.Service.AsignaturaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaturas")
public class AsignaturaController {

    private final AsignaturaService asignaturaService;

    public AsignaturaController(
            AsignaturaService asignaturaService) {

        this.asignaturaService = asignaturaService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping
    public ResponseEntity<List<AsignaturaResponse>> obtenerTodos() {

        return ResponseEntity.ok(
                asignaturaService.obtenerTodos()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/formacion/{formacionId}")
    public ResponseEntity<List<AsignaturaResponse>> obtenerPorFormacion(
            @PathVariable Long formacionId) {

        return ResponseEntity.ok(
                asignaturaService.obtenerPorFormacion(formacionId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/{id}")
    public ResponseEntity<AsignaturaResponse> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                asignaturaService.obtenerPorId(id)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AsignaturaResponse> crear(
            @Valid @RequestBody AsignaturaRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(asignaturaService.crear(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AsignaturaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AsignaturaRequest request) {

        return ResponseEntity.ok(
                asignaturaService.actualizar(id, request)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        asignaturaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
