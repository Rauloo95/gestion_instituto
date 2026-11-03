package com.baleras.api.Integration;

import com.baleras.api.Entity.*;
import com.baleras.api.Repository.*;

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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClaseRepositoryIntegrationTest {

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
    private ClaseRepository claseRepository;

    @Autowired
    private AsignaturaRepository asignaturaRepository;

    @Autowired
    private FormacionRepository formacionRepository;

    @Autowired
    private CentroRepository centroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    // =========================
    // TESTS CRUD
    // =========================

    @Test
    @DisplayName("Debe guardar una clase correctamente")
    void debeGuardarClase() {

        Asignatura asignatura = crearAsignatura();
        Usuario profesor = crearProfesor();

        Clase clase = crearClase("Matemáticas - Grupo A", asignatura, profesor);

        Clase guardada = claseRepository.saveAndFlush(clase);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getNombre()).isEqualTo("Matemáticas - Grupo A");
        assertThat(guardada.getGrupo()).isEqualTo("A");
        assertThat(guardada.getAula()).isEqualTo("101");
        assertThat(guardada.getActivo()).isTrue();
        assertThat(guardada.getAsignatura().getId()).isEqualTo(asignatura.getId());
        assertThat(guardada.getProfesor().getId()).isEqualTo(profesor.getId());
    }

    @Test
    @DisplayName("Debe encontrar una clase por su ID")
    void debeEncontrarClasePorId() {

        Clase clase = crearClaseCompleta();

        Clase encontrada = claseRepository.findById(clase.getId()).orElseThrow();

        assertThat(encontrada.getNombre()).isEqualTo(clase.getNombre());
        assertThat(encontrada.getAsignatura().getId())
                .isEqualTo(clase.getAsignatura().getId());
        assertThat(encontrada.getProfesor().getId())
                .isEqualTo(clase.getProfesor().getId());
    }

    @Test
    @DisplayName("Debe devolver todas las clases")
    void debeDevolverTodasLasClases() {

        Clase clase1 = crearClaseCompleta();
        Clase clase2 = crearClaseCompleta();

        List<Clase> clases = claseRepository.findAll();

        assertThat(clases).hasSize(2);
        assertThat(clases)
                .extracting(Clase::getId)
                .containsExactlyInAnyOrder(clase1.getId(), clase2.getId());
    }

    @Test
    @DisplayName("Debe actualizar una clase correctamente")
    void debeActualizarClase() {

        Clase clase = crearClaseCompleta();

        clase.setNombre("Matemáticas avanzadas");
        clase.setGrupo("B");
        clase.setAula("205");
        clase.setActivo(false);

        claseRepository.saveAndFlush(clase);

        Clase actualizada = claseRepository.findById(clase.getId()).orElseThrow();

        assertThat(actualizada.getNombre()).isEqualTo("Matemáticas avanzadas");
        assertThat(actualizada.getGrupo()).isEqualTo("B");
        assertThat(actualizada.getAula()).isEqualTo("205");
        assertThat(actualizada.getActivo()).isFalse();
    }

    @Test
    @DisplayName("Debe eliminar una clase correctamente")
    void debeEliminarClase() {

        Clase clase = crearClaseCompleta();

        Long id = clase.getId();

        claseRepository.deleteById(id);
        claseRepository.flush();

        Optional<Clase> resultado = claseRepository.findById(id);

        assertThat(resultado).isEmpty();
    }


    // =========================
    // TESTS MÉTODOS PERSONALIZADOS
    // =========================

    @Test
    @DisplayName("Debe comprobar si existe una clase por nombre y asignatura")
    void debeComprobarExistenciaPorNombreYAsignatura() {

        Asignatura asignatura = crearAsignatura();
        Usuario profesor = crearProfesor();

        crearClase("Matemáticas", asignatura, profesor);

        boolean existe = claseRepository.existsByNombreAndAsignaturaId(
                "Matemáticas",
                asignatura.getId()
        );

        boolean noExiste = claseRepository.existsByNombreAndAsignaturaId(
                "Física",
                asignatura.getId()
        );

        assertThat(existe).isTrue();
        assertThat(noExiste).isFalse();
    }

    @Test
    @DisplayName("Debe encontrar las clases de una asignatura concreta")
    void debeEncontrarClasesPorAsignaturaId() {

        Asignatura asignatura1 = crearAsignatura();
        Asignatura asignatura2 = crearAsignatura();
        Usuario profesor = crearProfesor();

        Clase clase1 = crearClase("Matemáticas - A", asignatura1, profesor);
        Clase clase2 = crearClase("Matemáticas - B", asignatura1, profesor);

        crearClase("Física - A", asignatura2, profesor);

        List<Clase> resultado = claseRepository.findByAsignaturaId(
                asignatura1.getId()
        );

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(Clase::getId)
                .containsExactlyInAnyOrder(clase1.getId(), clase2.getId());
    }

    @Test
    @DisplayName("Debe encontrar las clases de un profesor concreto")
    void debeEncontrarClasesPorProfesorId() {

        Asignatura asignatura = crearAsignatura();

        Usuario profesor1 = crearProfesor();
        Usuario profesor2 = crearProfesor();

        Clase clase1 = crearClase("Matemáticas", asignatura, profesor1);
        Clase clase2 = crearClase("Física", asignatura, profesor1);

        crearClase("Química", asignatura, profesor2);

        List<Clase> resultado = claseRepository.findByProfesorId(
                profesor1.getId()
        );

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(Clase::getId)
                .containsExactlyInAnyOrder(clase1.getId(), clase2.getId());
    }

    @Test
    @DisplayName("Debe devolver una lista vacía cuando no existen clases para una asignatura")
    void debeDevolverListaVaciaCuandoNoHayClasesParaAsignatura() {

        Asignatura asignatura = crearAsignatura();

        List<Clase> resultado = claseRepository.findByAsignaturaId(
                asignatura.getId()
        );

        assertThat(resultado).isEmpty();
    }


    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    private Centro crearCentro() {

        Centro centro = Centro.builder()
                .nombre("Centro de pruebas")
                .tipo("Educativo")
                .direccion("Calle de prueba 1")
                .ciudad("Madrid")
                .codigoPostal("28001")
                .activo(true)
                .build();

        return centroRepository.saveAndFlush(centro);
    }

    private Formacion crearFormacion() {

        Centro centro = crearCentro();

        Formacion formacion = Formacion.builder()
                .nombre("Desarrollo de aplicaciones")
                .descripcion("Formación de prueba")
                .nivel("Superior")
                .codigo("DAM")
                .centro(centro)
                .build();

        return formacionRepository.saveAndFlush(formacion);
    }

    private Asignatura crearAsignatura() {

        Formacion formacion = crearFormacion();

        Asignatura asignatura = Asignatura.builder()
                .nombre("Asignatura de prueba")
                .descripcion("Asignatura para tests")
                .codigo("TEST")
                .curso("Primero")
                .formacion(formacion)
                .build();

        return asignaturaRepository.saveAndFlush(asignatura);
    }

    private Usuario crearProfesor() {

        Usuario profesor = Usuario.builder()
                .nombre("Profesor")
                .apellidos("De Prueba")
                .email("profesor" + System.nanoTime() + "@test.com")
                .password("password123")
                .rol(Rol.PROFESOR)
                .build();

        return usuarioRepository.saveAndFlush(profesor);
    }

    private Clase crearClase(
            String nombre,
            Asignatura asignatura,
            Usuario profesor
    ) {

        Clase clase = Clase.builder()
                .nombre(nombre)
                .grupo("A")
                .aula("101")
                .activo(true)
                .asignatura(asignatura)
                .profesor(profesor)
                .build();

        return claseRepository.saveAndFlush(clase);
    }

    private Clase crearClaseCompleta() {

        Asignatura asignatura = crearAsignatura();
        Usuario profesor = crearProfesor();

        return crearClase("Clase de prueba", asignatura, profesor);
    }
}
