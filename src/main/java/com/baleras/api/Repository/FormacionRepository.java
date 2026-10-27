package com.baleras.api.Repository;

import com.baleras.api.Entity.Formacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormacionRepository extends JpaRepository<Formacion, Long> {
    boolean existsByNombreAndCentroId(String nombre, Long centroId);

    List<Formacion> findByCentroId(Long centroId);
}
