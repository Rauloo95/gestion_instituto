package com.baleras.api.Integration;

import com.baleras.api.Entity.*;
import com.baleras.api.Repository.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MatriculaRepositoryIntegrationTest {

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
    private MatriculaRepository matriculaRepository;

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
    @DisplayName("Debe guardar una matrícula correctamente")
    void debeGuardarMatricula() {

        Usuario alumno = crearUsuario(Rol.ALUMNO);
        Clase clase = crearClase();

        Matricula matricula = crearMatricula(alumno, clase);

        Matricula guardada = matriculaRepository.saveAndFlush(matricula);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getAlumno().getId()).isEqualTo(alumno.getId());
        assertThat(guardada.getClase().getId()).isEqualTo(clase.getId());
        assertThat(guardada.getFechaMatricula()).isEqualTo(LocalDate.now());
        assertThat(guardada.getEstado()).isEqualTo(EstadoMatricula.ACTIVA);
    }

    @Test
    @DisplayName("Debe encontrar una matrícula por su ID")
    void debeEncontrarMatriculaPorId() {

        Matricula matricula = crearMatriculaCompleta();

        Matricula encontrada = matriculaRepository
                .findById(matricula.getId())
                .orElseThrow();

        assertThat(encontrada.getAlumno().getId())
                .isEqualTo(matricula.getAlumno().getId());

        assertThat(encontrada.getClase().getId())
                .isEqualTo(matricula.getClase().getId());
    }

    @Test
    @DisplayName("Debe devolver todas las matrículas")
    void debeDevolverTodasLasMatriculas() {

        Matricula matricula1 = crearMatriculaCompleta();
        Matricula matricula2 = crearMatriculaCompleta();

        List<Matricula> resultado = matriculaRepository.findAll();

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(Matricula::getId)
                .containsExactlyInAnyOrder(
                        matricula1.getId(),
                        matricula2.getId()
                );
    }

    @Test
    @DisplayName("Debe actualizar el estado de una matrícula")
    void debeActualizarMatricula() {

        Matricula matricula = crearMatriculaCompleta();

        matricula.setEstado(EstadoMatricula.FINALIZADA);

        matriculaRepository.saveAndFlush(matricula);

        Matricula actualizada = matriculaRepository
                .findById(matricula.getId())
                .orElseThrow();

        assertThat(actualizada.getEstado())
                .isEqualTo(EstadoMatricula.FINALIZADA);
    }

    @Test
    @DisplayName("Debe eliminar una matrícula correctamente")
    void debeEliminarMatricula() {

        Matricula matricula = crearMatriculaCompleta();

        Long id = matricula.getId();

        matriculaRepository.deleteById(id);
        matriculaRepository.flush();

        Optional<Matricula> resultado = matriculaRepository.findById(id);

        assertThat(resultado).isEmpty();
    }


    // =========================
    // TESTS MÉTODOS PERSONALIZADOS
    // =========================

    @Test
    @DisplayName("Debe comprobar si existe una matrícula por alumno y clase")
    void debeComprobarExistenciaPorAlumnoYClase() {

        Matricula matricula = crearMatriculaCompleta();

        boolean existe = matriculaRepository.existsByAlumnoIdAndClaseId(
                matricula.getAlumno().getId(),
                matricula.getClase().getId()
        );

        boolean noExiste = matriculaRepository.existsByAlumnoIdAndClaseId(
                99999L,
                99999L
        );

        assertThat(existe).isTrue();
        assertThat(noExiste).isFalse();
    }

    @Test
    @DisplayName("Debe encontrar las matrículas de un alumno concreto")
    void debeEncontrarMatriculasPorAlumnoId() {

        Usuario alumno1 = crearUsuario(Rol.ALUMNO);
        Usuario alumno2 = crearUsuario(Rol.ALUMNO);

        Clase clase1 = crearClase();
        Clase clase2 = crearClase();
        Clase clase3 = crearClase();

        Matricula matricula1 = crearMatricula(alumno1, clase1);
        Matricula matricula2 = crearMatricula(alumno1, clase2);

        crearMatricula(alumno2, clase3);

        List<Matricula> resultado = matriculaRepository.findByAlumnoId(
                alumno1.getId()
        );

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(Matricula::getId)
                .containsExactlyInAnyOrder(
                        matricula1.getId(),
                        matricula2.getId()
                );
    }

    @Test
    @DisplayName("Debe encontrar las matrículas de una clase concreta")
    void debeEncontrarMatriculasPorClaseId() {

        Clase clase1 = crearClase();
        Clase clase2 = crearClase();

        Usuario alumno1 = crearUsuario(Rol.ALUMNO);
        Usuario alumno2 = crearUsuario(Rol.ALUMNO);
        Usuario alumno3 = crearUsuario(Rol.ALUMNO);

        Matricula matricula1 = crearMatricula(alumno1, clase1);
        Matricula matricula2 = crearMatricula(alumno2, clase1);

        crearMatricula(alumno3, clase2);

        List<Matricula> resultado = matriculaRepository.findByClaseId(
                clase1.getId()
        );

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(Matricula::getId)
                .containsExactlyInAnyOrder(
                        matricula1.getId(),
                        matricula2.getId()
                );
    }

    @Test
    @DisplayName("Debe encontrar matrículas por estado")
    void debeEncontrarMatriculasPorEstado() {

        Matricula activa = crearMatriculaCompleta();
        Matricula cancelada = crearMatriculaCompleta();

        cancelada.setEstado(EstadoMatricula.CANCELADA);
        matriculaRepository.saveAndFlush(cancelada);

        List<Matricula> resultado = matriculaRepository.findByEstado(
                EstadoMatricula.ACTIVA
        );

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(activa.getId());
    }

    @Test
    @DisplayName("Debe devolver una lista vacía cuando un alumno no tiene matrículas")
    void debeDevolverListaVaciaCuandoNoHayMatriculas() {

        Usuario alumno = crearUsuario(Rol.ALUMNO);

        List<Matricula> resultado = matriculaRepository.findByAlumnoId(
                alumno.getId()
        );

        assertThat(resultado).isEmpty();
    }


    // =========================
    // TEST DE RESTRICCIÓN UNIQUE
    // =========================

    @Test
    @DisplayName("No debe permitir matricular dos veces al mismo alumno en la misma clase")
    void noDebePermitirMatriculaDuplicada() {

        Usuario alumno = crearUsuario(Rol.ALUMNO);
        Clase clase = crearClase();

        crearMatricula(alumno, clase);

        Matricula duplicada = Matricula.builder()
                .alumno(alumno)
                .clase(clase)
                .fechaMatricula(LocalDate.now())
                .estado(EstadoMatricula.ACTIVA)
                .build();

        assertThatThrownBy(() -> matriculaRepository.saveAndFlush(duplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }


    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    private Usuario crearUsuario(Rol rol) {

        Usuario usuario = Usuario.builder()
                .nombre("Usuario")
                .apellidos("De Prueba")
                .email(rol.name().toLowerCase() + System.nanoTime() + "@test.com")
                .password("password123")
                .rol(rol)
                .build();

        return usuarioRepository.saveAndFlush(usuario);
    }

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

        Formacion formacion = Formacion.builder()
                .nombre("Desarrollo de aplicaciones")
                .descripcion("Formación de prueba")
                .nivel("Superior")
                .codigo("DAM")
                .centro(crearCentro())
                .build();

        return formacionRepository.saveAndFlush(formacion);
    }

    private Asignatura crearAsignatura() {

        Asignatura asignatura = Asignatura.builder()
                .nombre("Asignatura de prueba")
                .descripcion("Asignatura para tests")
                .codigo("TEST")
                .curso("Primero")
                .formacion(crearFormacion())
                .build();

        return asignaturaRepository.saveAndFlush(asignatura);
    }

    private Clase crearClase() {

        Clase clase = Clase.builder()
                .nombre("Clase de prueba")
                .grupo("A")
                .aula("101")
                .activo(true)
                .asignatura(crearAsignatura())
                .profesor(crearUsuario(Rol.PROFESOR))
                .build();

        return claseRepository.saveAndFlush(clase);
    }

    private Matricula crearMatricula(Usuario alumno, Clase clase) {

        Matricula matricula = Matricula.builder()
                .alumno(alumno)
                .clase(clase)
                .fechaMatricula(LocalDate.now())
                .estado(EstadoMatricula.ACTIVA)
                .build();

        return matriculaRepository.saveAndFlush(matricula);
    }

    private Matricula crearMatriculaCompleta() {

        Usuario alumno = crearUsuario(Rol.ALUMNO);
        Clase clase = crearClase();

        return crearMatricula(alumno, clase);
    }
}
