package com.baleras.api.Integration;

import com.baleras.api.Entity.Centro;
import com.baleras.api.Repository.CentroRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CentroRepositoryIntegrationTest {

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
    private CentroRepository centroRepository;

    private Centro crearCentro() {
        return Centro.builder()
                .nombre("Centro Baleras")
                .tipo("Educativo")
                .direccion("Calle Mayor 10")
                .ciudad("Madrid")
                .codigoPostal("28001")
                .telefono("600123456")
                .email("info@baleras.com")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Guardar centro en PostgreSQL")
    void guardarCentro_persisteCorrectamente() {

        Centro guardado = centroRepository.saveAndFlush(crearCentro());

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getNombre()).isEqualTo("Centro Baleras");
        assertThat(guardado.getTipo()).isEqualTo("Educativo");
        assertThat(guardado.getCiudad()).isEqualTo("Madrid");
        assertThat(guardado.getActivo()).isTrue();
    }

    @Test
    @DisplayName("Buscar centro por ID")
    void buscarPorId_centroExiste_devuelveCentro() {

        Centro guardado = centroRepository.saveAndFlush(crearCentro());

        Centro resultado = centroRepository.findById(guardado.getId())
                .orElseThrow();

        assertThat(resultado.getNombre()).isEqualTo("Centro Baleras");
        assertThat(resultado.getEmail()).isEqualTo("info@baleras.com");
    }

    @Test
    @DisplayName("Recuperar todos los centros")
    void findAll_devuelveCentros() {

        centroRepository.saveAndFlush(crearCentro());

        Centro segundoCentro = crearCentro();
        segundoCentro.setNombre("Centro Secundario");
        segundoCentro.setEmail("secundario@baleras.com");

        centroRepository.saveAndFlush(segundoCentro);

        assertThat(centroRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Actualizar centro")
    void actualizarCentro_modificaDatosCorrectamente() {

        Centro guardado = centroRepository.saveAndFlush(crearCentro());

        guardado.setNombre("Centro Baleras Actualizado");
        guardado.setCiudad("Alcorcón");

        Centro actualizado = centroRepository.saveAndFlush(guardado);

        assertThat(actualizado.getNombre())
                .isEqualTo("Centro Baleras Actualizado");

        assertThat(actualizado.getCiudad()).isEqualTo("Alcorcón");
    }

    @Test
    @DisplayName("Eliminar centro")
    void eliminarCentro_centroDejaDeExistir() {

        Centro guardado = centroRepository.saveAndFlush(crearCentro());

        centroRepository.delete(guardado);
        centroRepository.flush();

        assertThat(centroRepository.findById(guardado.getId())).isEmpty();
    }

    @Test
    @DisplayName("Comprobar existencia por nombre")
    void existsByNombre_centroExiste_devuelveTrue() {

        centroRepository.saveAndFlush(crearCentro());

        boolean existe = centroRepository.existsByNombre("Centro Baleras");

        assertThat(existe).isTrue();
        assertThat(centroRepository.existsByNombre("Centro Inexistente")).isFalse();
    }
}
