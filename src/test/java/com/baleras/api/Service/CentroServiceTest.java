package com.baleras.api.Service;

import com.baleras.api.Dto.CentroRequest;
import com.baleras.api.Dto.CentroResponse;
import com.baleras.api.Entity.Centro;
import com.baleras.api.Exception.DuplicateResourceException;
import com.baleras.api.Exception.ResourceNotFoundException;
import com.baleras.api.Repository.CentroRepository;

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
class CentroServiceTest {

    @Mock
    private CentroRepository centroRepository;

    @InjectMocks
    private CentroService centroService;

    private Centro centro;

    @BeforeEach
    void setUp() {

        centro = Centro.builder()
                .id(1L)
                .nombre("Centro Baleras")
                .tipo("Educativo")
                .direccion("Calle Mayor 10")
                .ciudad("Madrid")
                .codigoPostal("28001")
                .telefono("600123456")
                .email("centro@email.com")
                .activo(true)
                .build();
    }

    @Test
    void obtenerTodos_deberiaDevolverListaDeCentros() {

        when(centroRepository.findAll())
                .thenReturn(List.of(centro));

        List<CentroResponse> response = centroService.obtenerTodos();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Centro Baleras", response.get(0).nombre());
        assertEquals("Madrid", response.get(0).ciudad());

        verify(centroRepository).findAll();
    }

    @Test
    void obtenerPorId_deberiaDevolverCentro() {

        when(centroRepository.findById(1L))
                .thenReturn(Optional.of(centro));

        CentroResponse response = centroService.obtenerPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Centro Baleras", response.nombre());
        assertEquals("Educativo", response.tipo());
        assertTrue(response.activo());

        verify(centroRepository).findById(1L);
    }

    @Test
    void obtenerPorId_centroNoExiste_deberiaLanzarExcepcion() {

        when(centroRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> centroService.obtenerPorId(99L)
        );

        verify(centroRepository).findById(99L);
    }

    @Test
    void crear_nombreDuplicado_deberiaLanzarExcepcion() {

        CentroRequest request = new CentroRequest(
                "Centro Baleras",
                "Educativo",
                "Calle Mayor 10",
                "Madrid",
                "28001",
                "600123456",
                "centro@email.com"
        );

        when(centroRepository.existsByNombre("Centro Baleras"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> centroService.crear(request)
        );

        verify(centroRepository, never()).save(any(Centro.class));
    }

    @Test
    void crear_centroValido_deberiaGuardarCentro() {

        CentroRequest request = new CentroRequest(
                "Centro Baleras",
                "Educativo",
                "Calle Mayor 10",
                "Madrid",
                "28001",
                "600123456",
                "centro@email.com"
        );

        when(centroRepository.existsByNombre("Centro Baleras"))
                .thenReturn(false);

        when(centroRepository.save(any(Centro.class)))
                .thenReturn(centro);

        CentroResponse response = centroService.crear(request);

        assertNotNull(response);
        assertEquals("Centro Baleras", response.nombre());
        assertEquals("Madrid", response.ciudad());

        verify(centroRepository).existsByNombre("Centro Baleras");
        verify(centroRepository).save(any(Centro.class));
    }

    @Test
    void eliminar_centroExistente_deberiaEliminarlo() {

        when(centroRepository.findById(1L))
                .thenReturn(Optional.of(centro));

        centroService.eliminar(1L);

        verify(centroRepository).delete(centro);
    }
}
