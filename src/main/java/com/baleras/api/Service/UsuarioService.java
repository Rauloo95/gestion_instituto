package com.baleras.api.Service;

import com.baleras.api.Dto.UsuarioRequest;
import com.baleras.api.Dto.UsuarioResponse;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponse> obtenerTodos() {

        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public UsuarioResponse obtenerPorId(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el usuario con ID: " + id
                        )
                );

        return convertirAResponse(usuario);
    }

    public UsuarioResponse crear(UsuarioRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "Ya existe un usuario con el email: " + request.email()
            );
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .apellidos(request.apellidos())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return convertirAResponse(usuarioGuardado);
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se ha encontrado el usuario con ID: " + id
                        )
                );

        // Comprobamos que el nuevo email no pertenezca
        // a otro usuario
        if (!usuario.getEmail().equals(request.email())
                && usuarioRepository.existsByEmail(request.email())) {

            throw new DuplicateResourceException(
                    "Ya existe un usuario con el email: " + request.email()
            );
        }

        usuario.setNombre(request.nombre());
        usuario.setApellidos(request.apellidos());
        usuario.setEmail(request.email());
        usuario.setPassword(
                passwordEncoder.encode(request.password())
        );
        usuario.setRol(request.rol());

        Usuario usuarioActualizado =
                usuarioRepository.save(usuario);

        return convertirAResponse(usuarioActualizado);
    }

    public void eliminar(Long id) {

        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "No se ha encontrado el usuario con ID: " + id
            );
        }

        usuarioRepository.deleteById(id);
    }

    private UsuarioResponse convertirAResponse(Usuario usuario) {

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }
}