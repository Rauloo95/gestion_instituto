package com.baleras.api.Service;

import com.baleras.api.Dto.AsignaturaRequest;
import com.baleras.api.Dto.AsignaturaResponse;
import com.baleras.api.Entity.Asignatura;
import com.baleras.api.Entity.Centro;
import com.baleras.api.Entity.Formacion;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.AsignaturaRepository;
import com.baleras.api.Repository.FormacionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignaturaServiceTest {

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @Mock
    private FormacionRepository formacionRepository;

    @InjectMocks
    private AsignaturaService asignaturaService;

    private Centro centro;
    private Formacion formacion;
    private Asignatura asignatura;

    @BeforeEach
    void setUp() {

        centro = Centro.builder()
                .id(1L)
                .nombre("Centro Baleras")
                .tipo("Educativo")
                .activo(true)
                .build();

        formacion = Formacion.builder()
                .id(1L)
                .nombre("Desarrollo de Aplicaciones Multiplataforma")
                .nivel("Superior")
                .codigo("DAM")
                .centro(centro)
                .build();

        asignatura = Asignatura.builder()
                .id(1L)
                .nombre("Programación")
                .descripcion("Desarrollo de aplicaciones")
                .codigo("PROG")
                .curso("Primero")
                .formacion(formacion)
                .build();
    }

    @Test
    void obtenerTodos_deberiaDevolverAsignaturas() {

        when(asignaturaRepository.findAll())
                .thenReturn(List.of(asignatura));

        List<AsignaturaResponse> response =
                asignaturaService.obtenerTodos();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Programación", response.get(0).nombre());
        assertEquals(
                "Desarrollo de Aplicaciones Multiplataforma",
                response.get(0).formacionNombre()
        );

        verify(asignaturaRepository).findAll();
    }

    @Test
    void obtenerPorId_deberiaDevolverAsignatura() {

        when(asignaturaRepository.findById(1L))
                .thenReturn(Optional.of(asignatura));

        AsignaturaResponse response =
                asignaturaService.obtenerPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Programación", response.nombre());
        assertEquals("PROG", response.codigo());
        assertEquals(1L, response.formacionId());

        verify(asignaturaRepository).findById(1L);
    }

    @Test
    void obtenerPorId_asignaturaNoExiste_deberiaLanzarExcepcion() {

        when(asignaturaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> asignaturaService.obtenerPorId(99L)
        );

        verify(asignaturaRepository).findById(99L);
    }

    @Test
    void obtenerPorFormacion_deberiaDevolverAsignaturas() {

        when(formacionRepository.existsById(1L))
                .thenReturn(true);

        when(asignaturaRepository.findByFormacionId(1L))
                .thenReturn(List.of(asignatura));

        List<AsignaturaResponse> response =
                asignaturaService.obtenerPorFormacion(1L);

        assertEquals(1, response.size());
        assertEquals("Programación", response.get(0).nombre());

        verify(formacionRepository).existsById(1L);
        verify(asignaturaRepository).findByFormacionId(1L);
    }

    @Test
    void obtenerPorFormacion_formacionNoExiste_deberiaLanzarExcepcion() {

        when(formacionRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> asignaturaService.obtenerPorFormacion(99L)
        );

        verify(asignaturaRepository, never())
                .findByFormacionId(any());
    }

    @Test
    void crear_formacionNoExiste_deberiaLanzarExcepcion() {

        AsignaturaRequest request = new AsignaturaRequest(
                "Programación",
                "Desarrollo de aplicaciones",
                "PROG",
                "Primero",
                99L
        );

        when(formacionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> asignaturaService.crear(request)
        );

        verify(asignaturaRepository, never())
                .save(any(Asignatura.class));
    }

    @Test
    void crear_asignaturaDuplicada_deberiaLanzarExcepcion() {

        AsignaturaRequest request = new AsignaturaRequest(
                "Programación",
                "Desarrollo de aplicaciones",
                "PROG",
                "Primero",
                1L
        );

        when(formacionRepository.findById(1L))
                .thenReturn(Optional.of(formacion));

        when(asignaturaRepository.existsByNombreAndFormacionId(
                "Programación", 1L))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> asignaturaService.crear(request)
        );

        verify(asignaturaRepository, never())
                .save(any(Asignatura.class));
    }

    @Test
    void crear_asignaturaValida_deberiaGuardarAsignatura() {

        AsignaturaRequest request = new AsignaturaRequest(
                "Programación",
                "Desarrollo de aplicaciones",
                "PROG",
                "Primero",
                1L
        );

        when(formacionRepository.findById(1L))
                .thenReturn(Optional.of(formacion));

        when(asignaturaRepository.existsByNombreAndFormacionId(
                "Programación", 1L))
                .thenReturn(false);

        when(asignaturaRepository.save(any(Asignatura.class)))
                .thenReturn(asignatura);

        AsignaturaResponse response =
                asignaturaService.crear(request);

        assertNotNull(response);
        assertEquals("Programación", response.nombre());
        assertEquals(
                "Desarrollo de Aplicaciones Multiplataforma",
                response.formacionNombre()
        );
        assertEquals(1L, response.formacionId());

        verify(asignaturaRepository)
                .save(any(Asignatura.class));
    }

    @Test
    void eliminar_asignaturaExistente_deberiaEliminarla() {

        when(asignaturaRepository.findById(1L))
                .thenReturn(Optional.of(asignatura));

        asignaturaService.eliminar(1L);

        verify(asignaturaRepository).delete(asignatura);
    }
}