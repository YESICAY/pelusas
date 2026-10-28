package com.pelusas.backend.repository;

import com.pelusas.backend.entity.PartidaEntity;
import com.pelusas.backend.entity.ResultadoJugadorEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PartidaRepositoryTest {

    @Autowired
    private PartidaRepository repositorio;

    @AfterEach
    void limpiar() {
        repositorio.deleteAll();
    }

    private static PartidaEntity partidaDe(LocalDateTime fecha) {
        PartidaEntity partida = new PartidaEntity(fecha);
        partida.agregarJugador(new ResultadoJugadorEntity("Ana", "5,5,2", 12, true));
        partida.agregarJugador(new ResultadoJugadorEntity("Beto", "9", 9, false));
        return partida;
    }

    @Test
    void guardaYRecuperaUnaPartidaConSusJugadores() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 27, 15, 30);

        PartidaEntity guardada = repositorio.save(partidaDe(fecha));
        PartidaEntity recuperada = repositorio.findById(guardada.getId()).orElseThrow();

        assertNotNull(guardada.getId());
        assertEquals(fecha, recuperada.getFecha());
        assertEquals(2, recuperada.getJugadores().size());
        ResultadoJugadorEntity ana = recuperada.getJugadores().get(0);
        assertEquals("Ana", ana.getNombre());
        assertEquals("5,5,2", ana.getCartas());
        assertEquals(12, ana.getPuntuacion());
        assertTrue(ana.isGanador());
        ResultadoJugadorEntity beto = recuperada.getJugadores().get(1);
        assertEquals("Beto", beto.getNombre());
        assertEquals("9", beto.getCartas());
        assertEquals(9, beto.getPuntuacion());
        assertFalse(beto.isGanador());
    }

    @Test
    void cadaJugadorApuntaASuPartida() {
        PartidaEntity guardada = repositorio.save(partidaDe(LocalDateTime.of(2026, 10, 27, 15, 30)));

        PartidaEntity recuperada = repositorio.findById(guardada.getId()).orElseThrow();

        for (ResultadoJugadorEntity jugador : recuperada.getJugadores()) {
            assertEquals(guardada.getId(), jugador.getPartida().getId());
        }
    }

    @Test
    void elHistorialSeListaDeLaMasRecienteALaMasAntigua() {
        PartidaEntity antigua = repositorio.save(partidaDe(LocalDateTime.of(2026, 10, 1, 10, 0)));
        PartidaEntity reciente = repositorio.save(partidaDe(LocalDateTime.of(2026, 10, 20, 10, 0)));

        List<PartidaEntity> historial = repositorio.findAllByOrderByFechaDesc();

        assertEquals(2, historial.size());
        assertEquals(reciente.getId(), historial.get(0).getId());
        assertEquals(antigua.getId(), historial.get(1).getId());
    }
}