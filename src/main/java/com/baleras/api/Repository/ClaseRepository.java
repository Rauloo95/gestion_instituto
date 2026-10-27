package com.baleras.api.Repository;

import com.baleras.api.Entity.Clase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaseRepository extends JpaRepository<Clase, Long> {

    boolean existsByNombreAndAsignaturaId(
            String nombre,
            Long asignaturaId
    );

    List<Clase> findByAsignaturaId(Long asignaturaId);

    List<Clase> findByProfesorId(Long profesorId);
}
