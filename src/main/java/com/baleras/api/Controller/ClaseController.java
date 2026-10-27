package com.baleras.api.Controller;

import com.baleras.api.Dto.ClaseRequest;
import com.baleras.api.Dto.ClaseResponse;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Service.ClaseService;
import com.baleras.api.Service.UsuarioActualService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clases")
public class ClaseController {

    private final ClaseService claseService;
    private final UsuarioActualService usuarioActualService;

    public ClaseController(ClaseService claseService, UsuarioActualService usuarioActualService) {
        this.claseService = claseService;
        this.usuarioActualService = usuarioActualService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping
    public ResponseEntity<List<ClaseResponse>> obtenerTodos() {

        return ResponseEntity.ok(
                claseService.obtenerTodos()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<ClaseResponse>> obtenerPorAsignatura(
            @PathVariable Long asignaturaId) {

        return ResponseEntity.ok(
                claseService.obtenerPorAsignatura(asignaturaId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<List<ClaseResponse>> obtenerPorProfesor(
            @PathVariable Long profesorId) {

        return ResponseEntity.ok(
                claseService.obtenerPorProfesor(profesorId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ClaseResponse> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                claseService.obtenerPorId(id)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ClaseResponse> crear(
            @Valid @RequestBody ClaseRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(claseService.crear(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ClaseResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClaseRequest request) {

        return ResponseEntity.ok(
                claseService.actualizar(id, request)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        claseService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('PROFESOR')")
    @GetMapping("/mis-clases")
    public ResponseEntity<List<ClaseResponse>> obtenerMisClases() {

        Usuario usuarioActual =
                usuarioActualService.obtenerUsuarioActual();

        return ResponseEntity.ok(
                claseService.obtenerPorProfesor(usuarioActual.getId())
        );
    }
}
