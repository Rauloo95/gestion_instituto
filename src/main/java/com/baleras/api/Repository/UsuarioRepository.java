package com.baleras.api.Repository;

import com.baleras.api.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    //nueva consulta para evitar duplicados de email
    boolean existsByEmail(String email);

    //buscar por email para el auth
    Optional<Usuario> findByEmail(String email);
}
