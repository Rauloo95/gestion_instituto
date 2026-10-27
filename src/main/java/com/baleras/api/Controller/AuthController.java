package com.baleras.api.Controller;

import com.baleras.api.Dto.AuthRequest;
import com.baleras.api.Dto.AuthResponse;
import com.baleras.api.Dto.UsuarioRequest;
import com.baleras.api.Dto.UsuarioResponse;
import com.baleras.api.Service.AuthService;
import com.baleras.api.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(
            @Valid @RequestBody UsuarioRequest request) {

        UsuarioRequest registro = new UsuarioRequest(
                request.nombre(),
                request.apellidos(),
                request.email(),
                request.password(),
                com.baleras.api.Entity.Rol.ALUMNO
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.crear(registro));
    }
}
