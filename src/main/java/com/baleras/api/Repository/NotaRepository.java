package com.baleras.api.Repository;

import com.baleras.api.Entity.Nota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    Optional<Nota> findByMatriculaId(Long matriculaId);

    List<Nota> findByProfesorId(Long profesorId);

    List<Nota> findByMatriculaAlumnoId(Long alumnoId);
}
