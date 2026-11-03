package com.baleras.api.Service;

import com.baleras.api.Dto.FormacionRequest;
import com.baleras.api.Dto.FormacionResponse;
import com.baleras.api.Entity.Centro;
import com.baleras.api.Entity.Formacion;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.CentroRepository;
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
class FormacionServiceTest {

    @Mock
    private FormacionRepository formacionRepository;

    @Mock
    private CentroRepository centroRepository;

    @InjectMocks
    private FormacionService formacionService;

    private Centro centro;
    private Formacion formacion;

    @BeforeEach
    void setUp() {

        centro = Centro.builder()
                .id(1L)
                .nombre("Centro Baleras")
                .tipo("Educativo")
                .direccion("Calle Mayor 10")
                .ciudad("Madrid")
                .activo(true)
                .build();

        formacion = Formacion.builder()
                .id(1L)
                .nombre("Desarrollo de Aplicaciones Multiplataforma")
                .descripcion("Formación de desarrollo de software")
                .nivel("Superior")
                .codigo("DAM")
                .centro(centro)
                .build();
    }

    @Test
    void obtenerTodos_deberiaDevolverFormaciones() {

        when(formacionRepository.findAll())
                .thenReturn(List.of(formacion));

        List<FormacionResponse> response =
                formacionService.obtenerTodos();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(
                "Desarrollo de Aplicaciones Multiplataforma",
                response.get(0).nombre()
        );
        assertEquals("Centro Baleras", response.get(0).centroNombre());

        verify(formacionRepository).findAll();
    }

    @Test
    void obtenerPorId_deberiaDevolverFormacion() {

        when(formacionRepository.findById(1L))
                .thenReturn(Optional.of(formacion));

        FormacionResponse response =
                formacionService.obtenerPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("DAM", response.codigo());
        assertEquals(1L, response.centroId());

        verify(formacionRepository).findById(1L);
    }

    @Test
    void obtenerPorId_formacionNoExiste_deberiaLanzarExcepcion() {

        when(formacionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> formacionService.obtenerPorId(99L)
        );

        verify(formacionRepository).findById(99L);
    }

    @Test
    void obtenerPorCentro_deberiaDevolverFormacionesDelCentro() {

        when(centroRepository.existsById(1L))
                .thenReturn(true);

        when(formacionRepository.findByCentroId(1L))
                .thenReturn(List.of(formacion));

        List<FormacionResponse> response =
                formacionService.obtenerPorCentro(1L);

        assertEquals(1, response.size());
        assertEquals("DAM", response.get(0).codigo());

        verify(centroRepository).existsById(1L);
        verify(formacionRepository).findByCentroId(1L);
    }

    @Test
    void obtenerPorCentro_centroNoExiste_deberiaLanzarExcepcion() {

        when(centroRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> formacionService.obtenerPorCentro(99L)
        );

        verify(formacionRepository, never()).findByCentroId(any());
    }

    @Test
    void crear_centroNoExiste_deberiaLanzarExcepcion() {

        FormacionRequest request = new FormacionRequest(
                "DAM",
                "Formación de desarrollo",
                "Superior",
                "DAM",
                99L
        );

        when(centroRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> formacionService.crear(request)
        );

        verify(formacionRepository, never())
                .save(any(Formacion.class));
    }

    @Test
    void crear_formacionDuplicada_deberiaLanzarExcepcion() {

        FormacionRequest request = new FormacionRequest(
                "DAM",
                "Formación de desarrollo",
                "Superior",
                "DAM",
                1L
        );

        when(centroRepository.findById(1L))
                .thenReturn(Optional.of(centro));

        when(formacionRepository.existsByNombreAndCentroId(
                "DAM", 1L))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> formacionService.crear(request)
        );

        verify(formacionRepository, never())
                .save(any(Formacion.class));
    }

    @Test
    void crear_formacionValida_deberiaGuardarFormacion() {

        FormacionRequest request = new FormacionRequest(
                "DAM",
                "Formación de desarrollo",
                "Superior",
                "DAM",
                1L
        );

        when(centroRepository.findById(1L))
                .thenReturn(Optional.of(centro));

        when(formacionRepository.existsByNombreAndCentroId(
                "DAM", 1L))
                .thenReturn(false);

        when(formacionRepository.save(any(Formacion.class)))
                .thenReturn(formacion);

        FormacionResponse response =
                formacionService.crear(request);

        assertNotNull(response);
        assertEquals("DAM", response.codigo());
        assertEquals(1L, response.centroId());
        assertEquals("Centro Baleras", response.centroNombre());

        verify(formacionRepository)
                .save(any(Formacion.class));
    }

    @Test
    void eliminar_formacionExistente_deberiaEliminarla() {

        when(formacionRepository.findById(1L))
                .thenReturn(Optional.of(formacion));

        formacionService.eliminar(1L);

        verify(formacionRepository).delete(formacion);
    }
}
