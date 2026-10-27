package com.baleras.api.Repository;

import com.baleras.api.Entity.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsignaturaRepository
        extends JpaRepository<Asignatura, Long> {

    boolean existsByNombreAndFormacionId(
            String nombre,
            Long formacionId
    );

    List<Asignatura> findByFormacionId(Long formacionId);
}
