package com.baleras.api.Controller;

import com.baleras.api.Dto.CentroRequest;
import com.baleras.api.Dto.CentroResponse;
import com.baleras.api.Service.CentroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/centros")
public class CentroController {
    private final CentroService centroService;

    public CentroController(CentroService centroService) {
        this.centroService = centroService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping
    public ResponseEntity<List<CentroResponse>> obtenerTodos() {

        return ResponseEntity.ok(
                centroService.obtenerTodos()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/{id}")
    public ResponseEntity<CentroResponse> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                centroService.obtenerPorId(id)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CentroResponse> crear(
            @Valid @RequestBody CentroRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(centroService.crear(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CentroResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CentroRequest request) {

        return ResponseEntity.ok(
                centroService.actualizar(id, request)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        centroService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
