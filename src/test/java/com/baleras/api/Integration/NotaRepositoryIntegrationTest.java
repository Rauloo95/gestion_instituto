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

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotaRepositoryIntegrationTest {

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
    private NotaRepository notaRepository;

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
    @DisplayName("Debe guardar una nota correctamente")
    void debeGuardarNota() {

        Nota nota = crearNotaCompleta();

        assertThat(nota.getId()).isNotNull();
        assertThat(nota.getValor()).isEqualTo(8.5);
        assertThat(nota.getObservaciones()).isEqualTo("Buen trabajo");
        assertThat(nota.getFechaEvaluacion()).isNotNull();
        assertThat(nota.getMatricula().getId()).isNotNull();
        assertThat(nota.getProfesor().getId()).isNotNull();
    }

    @Test
    @DisplayName("Debe encontrar una nota por su ID")
    void debeEncontrarNotaPorId() {

        Nota nota = crearNotaCompleta();

        Nota encontrada = notaRepository.findById(nota.getId()).orElseThrow();

        assertThat(encontrada.getValor()).isEqualTo(nota.getValor());
        assertThat(encontrada.getMatricula().getId())
                .isEqualTo(nota.getMatricula().getId());
        assertThat(encontrada.getProfesor().getId())
                .isEqualTo(nota.getProfesor().getId());
    }

    @Test
    @DisplayName("Debe devolver todas las notas")
    void debeDevolverTodasLasNotas() {

        Nota nota1 = crearNotaCompleta();
        Nota nota2 = crearNotaCompleta();

        List<Nota> resultado = notaRepository.findAll();

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(Nota::getId)
                .containsExactlyInAnyOrder(nota1.getId(), nota2.getId());
    }

    @Test
    @DisplayName("Debe actualizar una nota correctamente")
    void debeActualizarNota() {

        Nota nota = crearNotaCompleta();

        nota.setValor(9.5);
        nota.setObservaciones("Excelente trabajo");

        notaRepository.saveAndFlush(nota);

        Nota actualizada = notaRepository.findById(nota.getId()).orElseThrow();

        assertThat(actualizada.getValor()).isEqualTo(9.5);
        assertThat(actualizada.getObservaciones())
                .isEqualTo("Excelente trabajo");
    }

    @Test
    @DisplayName("Debe eliminar una nota correctamente")
    void debeEliminarNota() {

        Nota nota = crearNotaCompleta();

        Long id = nota.getId();

        notaRepository.deleteById(id);
        notaRepository.flush();

        Optional<Nota> resultado = notaRepository.findById(id);

        assertThat(resultado).isEmpty();
    }


    // =========================
    // TESTS MÉTODOS PERSONALIZADOS
    // =========================

    @Test
    @DisplayName("Debe encontrar una nota por matrícula")
    void debeEncontrarNotaPorMatriculaId() {

        Nota nota = crearNotaCompleta();

        Optional<Nota> resultado = notaRepository.findByMatriculaId(
                nota.getMatricula().getId()
        );

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(nota.getId());
    }

    @Test
    @DisplayName("Debe devolver vacío cuando una matrícula no tiene nota")
    void debeDevolverVacioCuandoNoExisteNotaParaMatricula() {

        Usuario alumno = crearUsuario(Rol.ALUMNO);
        Clase clase = crearClase();

        Matricula matricula = crearMatricula(alumno, clase);

        Optional<Nota> resultado = notaRepository.findByMatriculaId(
                matricula.getId()
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe encontrar las notas de un profesor concreto")
    void debeEncontrarNotasPorProfesorId() {

        Usuario profesor1 = crearUsuario(Rol.PROFESOR);
        Usuario profesor2 = crearUsuario(Rol.PROFESOR);

        Nota nota1 = crearNota(crearUsuario(Rol.ALUMNO), profesor1, crearClase());
        Nota nota2 = crearNota(crearUsuario(Rol.ALUMNO), profesor1, crearClase());

        crearNota(crearUsuario(Rol.ALUMNO), profesor2, crearClase());

        List<Nota> resultado = notaRepository.findByProfesorId(profesor1.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(Nota::getId)
                .containsExactlyInAnyOrder(nota1.getId(), nota2.getId());
    }

    @Test
    @DisplayName("Debe encontrar las notas de un alumno concreto")
    void debeEncontrarNotasPorAlumnoId() {

        Usuario alumno1 = crearUsuario(Rol.ALUMNO);
        Usuario alumno2 = crearUsuario(Rol.ALUMNO);
        Usuario profesor = crearUsuario(Rol.PROFESOR);

        Nota nota1 = crearNota(alumno1, profesor, crearClase());
        Nota nota2 = crearNota(alumno1, profesor, crearClase());

        crearNota(alumno2, profesor, crearClase());

        List<Nota> resultado = notaRepository.findByMatriculaAlumnoId(
                alumno1.getId()
        );

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(Nota::getId)
                .containsExactlyInAnyOrder(nota1.getId(), nota2.getId());
    }


    // =========================
    // TEST RESTRICCIÓN UNIQUE
    // =========================

    @Test
    @DisplayName("No debe permitir dos notas para la misma matrícula")
    void noDebePermitirDosNotasParaLaMismaMatricula() {

        Nota nota = crearNotaCompleta();

        Nota duplicada = Nota.builder()
                .valor(7.0)
                .observaciones("Nota duplicada")
                .fechaEvaluacion(LocalDateTime.now())
                .matricula(nota.getMatricula())
                .profesor(nota.getProfesor())
                .build();

        assertThatThrownBy(() -> notaRepository.saveAndFlush(duplicada))
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

    private Nota crearNota(Usuario alumno, Usuario profesor, Clase clase) {

        Matricula matricula = crearMatricula(alumno, clase);

        Nota nota = Nota.builder()
                .valor(8.5)
                .observaciones("Buen trabajo")
                .fechaEvaluacion(LocalDateTime.now())
                .matricula(matricula)
                .profesor(profesor)
                .build();

        return notaRepository.saveAndFlush(nota);
    }

    private Nota crearNotaCompleta() {

        Usuario alumno = crearUsuario(Rol.ALUMNO);
        Usuario profesor = crearUsuario(Rol.PROFESOR);
        Clase clase = crearClase();

        return crearNota(alumno, profesor, clase);
    }
}
