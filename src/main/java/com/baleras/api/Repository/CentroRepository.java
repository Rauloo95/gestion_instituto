package com.baleras.api.Repository;

import com.baleras.api.Entity.Centro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CentroRepository extends JpaRepository<Centro, Long> {
    boolean existsByNombre(String nombre);

}
