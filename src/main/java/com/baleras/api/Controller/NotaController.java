package com.baleras.api.Controller;

import com.baleras.api.Dto.NotaRequest;
import com.baleras.api.Dto.NotaResponse;
import com.baleras.api.Service.NotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notas")
public class NotaController {

        private final NotaService notaService;

        public NotaController(NotaService notaService) {
                this.notaService = notaService;
        }

        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
        @GetMapping
        public ResponseEntity<List<NotaResponse>> obtenerTodos() {
                return ResponseEntity.ok(
                                notaService.obtenerTodos());
        }

        @PreAuthorize("hasRole('ALUMNO')")
        @GetMapping("/mis-notas")
        public ResponseEntity<List<NotaResponse>> obtenerMisNotas() {
                return ResponseEntity.ok(
                                notaService.obtenerMisNotas());
        }

        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'ALUMNO')")
        @GetMapping("/{id}")
        public ResponseEntity<NotaResponse> obtenerPorId(
                        @PathVariable Long id) {
                return ResponseEntity.ok(
                                notaService.obtenerPorId(id));
        }

        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'ALUMNO')")
        @GetMapping("/matricula/{matriculaId}")
        public ResponseEntity<NotaResponse> obtenerPorMatricula(
                        @PathVariable Long matriculaId) {
                return ResponseEntity.ok(
                                notaService.obtenerPorMatricula(matriculaId));
        }

        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'ALUMNO')")
        @GetMapping("/alumno/{alumnoId}")
        public ResponseEntity<List<NotaResponse>> obtenerPorAlumno(
                        @PathVariable Long alumnoId) {
                return ResponseEntity.ok(
                                notaService.obtenerPorAlumno(alumnoId));
        }

        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
        @GetMapping("/profesor/{profesorId}")
        public ResponseEntity<List<NotaResponse>> obtenerPorProfesor(
                        @PathVariable Long profesorId) {
                return ResponseEntity.ok(
                                notaService.obtenerPorProfesor(profesorId));
        }

        @PreAuthorize("hasRole('PROFESOR')")
        @PostMapping
        public ResponseEntity<NotaResponse> crear(
                        @Valid @RequestBody NotaRequest request) {
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(notaService.crear(request));
        }

        @PreAuthorize("hasRole('PROFESOR')")
        @PutMapping("/{id}")
        public ResponseEntity<NotaResponse> actualizar(
                        @PathVariable Long id,
                        @Valid @RequestBody NotaRequest request) {
                return ResponseEntity.ok(
                                notaService.actualizar(id, request));
        }

        @PreAuthorize("hasRole('PROFESOR')")
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminar(
                        @PathVariable Long id) {
                notaService.eliminar(id);
                return ResponseEntity.noContent().build();
        }
}
