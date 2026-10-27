package com.baleras.api.Repository;

import com.baleras.api.Entity.EstadoMatricula;
import com.baleras.api.Entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaRepository
        extends JpaRepository<Matricula, Long> {

    boolean existsByAlumnoIdAndClaseId(
            Long alumnoId,
            Long claseId
    );

    List<Matricula> findByAlumnoId(Long alumnoId);

    List<Matricula> findByClaseId(Long claseId);

    List<Matricula> findByEstado(EstadoMatricula estado);
}
