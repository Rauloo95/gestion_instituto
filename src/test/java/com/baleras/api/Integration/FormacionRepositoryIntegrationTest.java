package com.baleras.api.Integration;

import com.baleras.api.Entity.Centro;
import com.baleras.api.Entity.Formacion;
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
class FormacionRepositoryIntegrationTest {

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
    private FormacionRepository formacionRepository;

    @Autowired
    private CentroRepository centroRepository;

    private Centro crearCentro() {
        return centroRepository.saveAndFlush(
                Centro.builder()
                        .nombre("Centro Baleras")
                        .tipo("Educativo")
                        .ciudad("Madrid")
                        .activo(true)
                        .build()
        );
    }

    private Formacion crearFormacion(Centro centro) {
        return Formacion.builder()
                .nombre("Desarrollo de Aplicaciones Multiplataforma")
                .descripcion("Formación de desarrollo de software")
                .nivel("Superior")
                .codigo("DAM")
                .centro(centro)
                .build();
    }

    @Test
    @DisplayName("Guardar formación asociada a un centro")
    void guardarFormacion_persisteCorrectamente() {

        Centro centro = crearCentro();

        Formacion guardada = formacionRepository.saveAndFlush(
                crearFormacion(centro)
        );

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getNombre())
                .isEqualTo("Desarrollo de Aplicaciones Multiplataforma");
        assertThat(guardada.getCodigo()).isEqualTo("DAM");
        assertThat(guardada.getCentro().getId()).isEqualTo(centro.getId());
    }

    @Test
    @DisplayName("Buscar formación por ID")
    void buscarPorId_formacionExiste_devuelveFormacion() {

        Centro centro = crearCentro();

        Formacion guardada = formacionRepository.saveAndFlush(
                crearFormacion(centro)
        );

        Formacion resultado = formacionRepository.findById(guardada.getId())
                .orElseThrow();

        assertThat(resultado.getNombre()).isEqualTo(guardada.getNombre());
        assertThat(resultado.getCentro().getNombre()).isEqualTo("Centro Baleras");
    }

    @Test
    @DisplayName("Recuperar todas las formaciones")
    void findAll_devuelveFormaciones() {

        Centro centro = crearCentro();

        formacionRepository.saveAndFlush(crearFormacion(centro));

        Formacion segunda = crearFormacion(centro);
        segunda.setNombre("Desarrollo de Aplicaciones Web");
        segunda.setCodigo("DAW");

        formacionRepository.saveAndFlush(segunda);

        assertThat(formacionRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Actualizar formación")
    void actualizarFormacion_modificaDatosCorrectamente() {

        Centro centro = crearCentro();

        Formacion guardada = formacionRepository.saveAndFlush(
                crearFormacion(centro)
        );

        guardada.setNombre("Desarrollo actualizado");
        guardada.setNivel("Medio");

        Formacion actualizada = formacionRepository.saveAndFlush(guardada);

        assertThat(actualizada.getNombre()).isEqualTo("Desarrollo actualizado");
        assertThat(actualizada.getNivel()).isEqualTo("Medio");
    }

    @Test
    @DisplayName("Eliminar formación")
    void eliminarFormacion_formacionDejaDeExistir() {

        Centro centro = crearCentro();

        Formacion guardada = formacionRepository.saveAndFlush(
                crearFormacion(centro)
        );

        formacionRepository.delete(guardada);
        formacionRepository.flush();

        assertThat(formacionRepository.findById(guardada.getId())).isEmpty();
    }

    @Test
    @DisplayName("Comprobar existencia por nombre y centro")
    void existsByNombreAndCentroId_formacionExiste_devuelveTrue() {

        Centro centro = crearCentro();

        formacionRepository.saveAndFlush(crearFormacion(centro));

        assertThat(formacionRepository.existsByNombreAndCentroId(
                "Desarrollo de Aplicaciones Multiplataforma",
                centro.getId()
        )).isTrue();

        assertThat(formacionRepository.existsByNombreAndCentroId(
                "Formacion inexistente",
                centro.getId()
        )).isFalse();
    }

    @Test
    @DisplayName("Buscar formaciones por centro")
    void findByCentroId_devuelveSoloFormacionesDelCentro() {

        Centro centro1 = crearCentro();

        Centro centro2 = centroRepository.saveAndFlush(
                Centro.builder()
                        .nombre("Centro Secundario")
                        .tipo("Educativo")
                        .ciudad("Alcorcón")
                        .activo(true)
                        .build()
        );

        formacionRepository.saveAndFlush(crearFormacion(centro1));

        Formacion otra = crearFormacion(centro2);
        otra.setNombre("Administración de Sistemas");
        otra.setCodigo("ASIR");

        formacionRepository.saveAndFlush(otra);

        assertThat(formacionRepository.findByCentroId(centro1.getId()))
                .hasSize(1)
                .allMatch(f -> f.getCentro().getId().equals(centro1.getId()));
    }

    @Test
    @DisplayName("Buscar formaciones de un centro sin formaciones")
    void findByCentroId_sinFormaciones_devuelveListaVacia() {

        Centro centro = crearCentro();

        assertThat(formacionRepository.findByCentroId(centro.getId()))
                .isEmpty();
    }
}
