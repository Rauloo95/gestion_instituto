package com.baleras.api.Service;

import com.baleras.api.Entity.Usuario;
import com.baleras.api.Exception.InvalidCredentialException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioActualService {

    public Usuario obtenerUsuarioActual() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Usuario usuario)) {

            throw new InvalidCredentialException(
                    "No se ha podido identificar al usuario autenticado"
            );
        }

        return usuario;
    }
}
