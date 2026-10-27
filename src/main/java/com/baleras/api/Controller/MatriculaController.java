package com.baleras.api.Controller;

import com.baleras.api.Dto.MatriculaRequest;
import com.baleras.api.Dto.MatriculaResponse;
import com.baleras.api.Service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(
            MatriculaService matriculaService) {

        this.matriculaService = matriculaService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping
    public ResponseEntity<List<MatriculaResponse>> obtenerTodos() {

        return ResponseEntity.ok(
                matriculaService.obtenerTodos()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/{id}")
    public ResponseEntity<MatriculaResponse> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                matriculaService.obtenerPorId(id)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<List<MatriculaResponse>> obtenerPorAlumno(
            @PathVariable Long alumnoId) {

        return ResponseEntity.ok(
                matriculaService.obtenerPorAlumno(alumnoId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/clase/{claseId}")
    public ResponseEntity<List<MatriculaResponse>> obtenerPorClase(
            @PathVariable Long claseId) {

        return ResponseEntity.ok(
                matriculaService.obtenerPorClase(claseId)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MatriculaResponse> crear(
            @Valid @RequestBody MatriculaRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(matriculaService.crear(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MatriculaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MatriculaRequest request) {

        return ResponseEntity.ok(
                matriculaService.actualizar(id, request)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        matriculaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
