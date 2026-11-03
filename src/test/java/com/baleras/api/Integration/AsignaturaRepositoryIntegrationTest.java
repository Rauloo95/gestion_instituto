package com.baleras.api.Integration;

import com.baleras.api.Entity.Asignatura;
import com.baleras.api.Entity.Centro;
import com.baleras.api.Entity.Formacion;
import com.baleras.api.Repository.AsignaturaRepository;
import com.baleras.api.Repository.CentroRepository;
import com.baleras.api.Repository.FormacionRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AsignaturaRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("baleras_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private AsignaturaRepository asignaturaRepository;

    @Autowired
    private FormacionRepository formacionRepository;

    @Autowired
    private CentroRepository centroRepository;

    private Formacion crearFormacion() {

        Centro centro = centroRepository.saveAndFlush(
                Centro.builder()
                        .nombre("Centro Baleras")
                        .tipo("Educativo")
                        .ciudad("Madrid")
                        .activo(true)
                        .build()
        );

        return formacionRepository.saveAndFlush(
                Formacion.builder()
                        .nombre("Desarrollo de Aplicaciones Multiplataforma")
                        .descripcion("Formación de desarrollo de software")
                        .nivel("Superior")
                        .codigo("DAM")
                        .centro(centro)
                        .build()
        );
    }

    private Asignatura crearAsignatura(Formacion formacion) {
        return Asignatura.builder()
                .nombre("Programación")
                .descripcion("Fundamentos de programación")
                .codigo("PROG")
                .curso("Primero")
                .formacion(formacion)
                .build();
    }

    @Test
    @DisplayName("Guardar asignatura asociada a una formación")
    void guardarAsignatura_persisteCorrectamente() {

        Formacion formacion = crearFormacion();

        Asignatura guardada = asignaturaRepository.saveAndFlush(
                crearAsignatura(formacion)
        );

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getNombre()).isEqualTo("Programación");
        assertThat(guardada.getCodigo()).isEqualTo("PROG");
        assertThat(guardada.getCurso()).isEqualTo("Primero");
        assertThat(guardada.getFormacion().getId()).isEqualTo(formacion.getId());
    }

    @Test
    @DisplayName("Buscar asignatura por ID")
    void buscarPorId_asignaturaExiste_devuelveAsignatura() {

        Formacion formacion = crearFormacion();

        Asignatura guardada = asignaturaRepository.saveAndFlush(
                crearAsignatura(formacion)
        );

        Asignatura resultado = asignaturaRepository.findById(guardada.getId())
                .orElseThrow();

        assertThat(resultado.getNombre()).isEqualTo("Programación");
        assertThat(resultado.getFormacion().getNombre())
                .isEqualTo("Desarrollo de Aplicaciones Multiplataforma");
    }

    @Test
    @DisplayName("Recuperar todas las asignaturas")
    void findAll_devuelveAsignaturas() {

        Formacion formacion = crearFormacion();

        asignaturaRepository.saveAndFlush(crearAsignatura(formacion));

        Asignatura segunda = crearAsignatura(formacion);
        segunda.setNombre("Bases de Datos");
        segunda.setCodigo("BD");

        asignaturaRepository.saveAndFlush(segunda);

        assertThat(asignaturaRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Actualizar asignatura")
    void actualizarAsignatura_modificaDatosCorrectamente() {

        Formacion formacion = crearFormacion();

        Asignatura guardada = asignaturaRepository.saveAndFlush(
                crearAsignatura(formacion)
        );

        guardada.setNombre("Programación avanzada");
        guardada.setCurso("Segundo");

        Asignatura actualizada = asignaturaRepository.saveAndFlush(guardada);

        assertThat(actualizada.getNombre()).isEqualTo("Programación avanzada");
        assertThat(actualizada.getCurso()).isEqualTo("Segundo");
    }

    @Test
    @DisplayName("Eliminar asignatura")
    void eliminarAsignatura_asignaturaDejaDeExistir() {

        Formacion formacion = crearFormacion();

        Asignatura guardada = asignaturaRepository.saveAndFlush(
                crearAsignatura(formacion)
        );

        asignaturaRepository.delete(guardada);
        asignaturaRepository.flush();

        assertThat(asignaturaRepository.findById(guardada.getId())).isEmpty();
    }

    @Test
    @DisplayName("Comprobar existencia por nombre y formación")
    void existsByNombreAndFormacionId_asignaturaExiste_devuelveTrue() {

        Formacion formacion = crearFormacion();

        asignaturaRepository.saveAndFlush(crearAsignatura(formacion));

        assertThat(asignaturaRepository.existsByNombreAndFormacionId(
                "Programación",
                formacion.getId()
        )).isTrue();

        assertThat(asignaturaRepository.existsByNombreAndFormacionId(
                "Asignatura inexistente",
                formacion.getId()
        )).isFalse();
    }

    @Test
    @DisplayName("Buscar asignaturas por formación")
    void findByFormacionId_devuelveSoloAsignaturasDeLaFormacion() {

        Formacion formacion1 = crearFormacion();

        Formacion formacion2 = crearFormacion();
        formacion2.setNombre("Desarrollo de Aplicaciones Web");
        formacion2.setCodigo("DAW");
        formacion2 = formacionRepository.saveAndFlush(formacion2);

        asignaturaRepository.saveAndFlush(crearAsignatura(formacion1));

        Asignatura otra = crearAsignatura(formacion2);
        otra.setNombre("Diseño de Interfaces");
        otra.setCodigo("DI");

        asignaturaRepository.saveAndFlush(otra);

        assertThat(asignaturaRepository.findByFormacionId(formacion1.getId()))
                .hasSize(1)
                .allMatch(a -> a.getFormacion().getId().equals(formacion1.getId()));
    }

    @Test
    @DisplayName("Buscar asignaturas de una formación sin asignaturas")
    void findByFormacionId_sinAsignaturas_devuelveListaVacia() {

        Formacion formacion = crearFormacion();

        assertThat(asignaturaRepository.findByFormacionId(formacion.getId()))
                .isEmpty();
    }
}
