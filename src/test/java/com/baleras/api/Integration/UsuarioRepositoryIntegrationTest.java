package com.baleras.api.Integration;

import com.baleras.api.Entity.Rol;
import com.baleras.api.Entity.Usuario;
import com.baleras.api.Repository.UsuarioRepository;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryIntegrationTest {

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
    private UsuarioRepository usuarioRepository;

    private Usuario crearUsuario() {
        return Usuario.builder()
                .nombre("Raúl")
                .apellidos("Gómez")
                .email("raul@test.com")
                .password("password123")
                .rol(Rol.ADMIN)
                .build();
    }

    @Test
    @DisplayName("Guardar usuario en PostgreSQL")
    void guardarUsuario_persisteCorrectamente() {

        Usuario usuario = crearUsuario();

        Usuario guardado = usuarioRepository.saveAndFlush(usuario);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getNombre()).isEqualTo("Raúl");
        assertThat(guardado.getApellidos()).isEqualTo("Gómez");
        assertThat(guardado.getEmail()).isEqualTo("raul@test.com");
        assertThat(guardado.getRol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    @DisplayName("Buscar usuario por ID")
    void buscarPorId_usuarioExiste_devuelveUsuario() {

        Usuario guardado = usuarioRepository.saveAndFlush(crearUsuario());

        Optional<Usuario> resultado =
                usuarioRepository.findById(guardado.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("raul@test.com");
    }

    @Test
    @DisplayName("Buscar usuario por email")
    void buscarPorEmail_usuarioExiste_devuelveUsuario() {

        usuarioRepository.saveAndFlush(crearUsuario());

        Optional<Usuario> resultado =
                usuarioRepository.findByEmail("raul@test.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Raúl");
    }

    @Test
    @DisplayName("Comprobar si existe un usuario por email")
    void existsByEmail_usuarioExiste_devuelveTrue() {

        usuarioRepository.saveAndFlush(crearUsuario());

        boolean existe = usuarioRepository.existsByEmail("raul@test.com");

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("Buscar usuario por email inexistente")
    void buscarPorEmail_usuarioNoExiste_devuelveVacio() {

        Optional<Usuario> resultado =
                usuarioRepository.findByEmail("inexistente@test.com");

        assertThat(resultado).isEmpty();
    }
}
