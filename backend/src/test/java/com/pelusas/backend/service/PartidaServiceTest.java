package com.pelusas.backend.service;

import com.pelusas.backend.domain.Carta;
import com.pelusas.backend.domain.Mazo;
import com.pelusas.backend.dto.HistorialDto;
import com.pelusas.backend.dto.JugadorDto;
import com.pelusas.backend.dto.JugadorHistorialDto;
import com.pelusas.backend.dto.PartidaDto;
import com.pelusas.backend.dto.SacarCartaDto;
import com.pelusas.backend.entity.PartidaEntity;
import com.pelusas.backend.entity.ResultadoJugadorEntity;
import com.pelusas.backend.exception.PartidaNoEncontradaException;
import com.pelusas.backend.repository.PartidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartidaServiceTest {

    @Mock
    private PartidaRepository repositorio;

    @Mock
    private MazoFactory mazoFactory;

    private PartidaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PartidaService(repositorio, mazoFactory);
    }

    private PartidaDto crearConMazo(int... valores) {
        List<Carta> cartas = new ArrayList<>();
        for (int valor : valores) {
            cartas.add(valor == 0 ? Carta.pelusaEnvenenada() : new Carta(valor));
        }
        when(mazoFactory.crear()).thenReturn(new Mazo(cartas));
        return servicio.crear(List.of("Ana", "Beto"));
    }

    @Test
    void crearPartidaDevuelveElEstadoInicial() {
        PartidaDto partida = crearConMazo(1, 2, 3);

        assertEquals(1L, partida.id());
        assertEquals("Ana", partida.turnoDe());
        assertEquals(3, partida.cartasRestantesEnMazo());
        assertFalse(partida.terminada());
        assertEquals(2, partida.jugadores().size());
        assertTrue(partida.ganadores().isEmpty());
    }

    @Test
    void crearPartidaConUnSoloJugadorLanzaExcepcion() {
        when(mazoFactory.crear()).thenReturn(new Mazo(List.of(new Carta(1))));

        assertThrows(IllegalArgumentException.class, () -> servicio.crear(List.of("Ana")));
    }

    @Test
    void consultarUnaPartidaInexistenteLanzaExcepcion() {
        assertThrows(PartidaNoEncontradaException.class, () -> servicio.consultar(99L));
    }

    @Test
    void jugadoresDevuelveLosJugadoresDeLaPartida() {
        crearConMazo(1, 2);

        List<JugadorDto> jugadores = servicio.jugadores(1L);

        assertEquals(2, jugadores.size());
        assertEquals("Ana", jugadores.get(0).nombre());
        assertEquals("Beto", jugadores.get(1).nombre());
    }

    @Test
    void sacarCartaDeUnJugadorSinTurnoLanzaExcepcion() {
        crearConMazo(5, 9);

        assertThrows(IllegalStateException.class, () -> servicio.sacarCarta(1L, "Beto"));
    }

    @Test
    void sacarCartaPonePelusaDelanteDelJugadorYContinua() {
        crearConMazo(5, 9);

        SacarCartaDto respuesta = servicio.sacarCarta(1L, "Ana");

        assertEquals("CONTINUA", respuesta.resultado());
        assertEquals("Ana", respuesta.partida().turnoDe());
        assertEquals(Map.of(5, 1), respuesta.partida().jugadores().get(0).cartasDelante());
        assertEquals(1, respuesta.partida().cartasRestantesEnMazo());
    }

    @Test
    void repetirConTresCartasPierdeElTurnoYPasaAlSiguiente() {
        crearConMazo(5, 2, 3, 5, 9);
        servicio.sacarCarta(1L, "Ana");
        servicio.sacarCarta(1L, "Ana");
        servicio.sacarCarta(1L, "Ana");

        SacarCartaDto respuesta = servicio.sacarCarta(1L, "Ana");

        assertEquals("TURNO_PERDIDO", respuesta.resultado());
        assertEquals("Beto", respuesta.partida().turnoDe());
        JugadorDto ana = respuesta.partida().jugadores().get(0);
        assertTrue(ana.cartasDelante().isEmpty());
        assertEquals(0, ana.puntuacion());
        assertFalse(respuesta.partida().terminada());
    }

    @Test
    void terminarTurnoPasaAlSiguienteYConservaLasCartas() {
        crearConMazo(5, 9);
        servicio.sacarCarta(1L, "Ana");

        PartidaDto partida = servicio.terminarTurno(1L, "Ana");

        assertEquals("Beto", partida.turnoDe());
        assertEquals(Map.of(5, 1), partida.jugadores().get(0).cartasDelante());
    }

    @Test
    void guardarCartasMueveLasPelusasAlMonton() {
        crearConMazo(5, 9, 2);
        servicio.sacarCarta(1L, "Ana");
        servicio.terminarTurno(1L, "Ana");
        servicio.sacarCarta(1L, "Beto");
        servicio.terminarTurno(1L, "Beto");

        PartidaDto partida = servicio.guardarCartas(1L, "Ana");

        JugadorDto ana = partida.jugadores().get(0);
        assertTrue(ana.cartasDelante().isEmpty());
        assertEquals(1, ana.cartasEnMonton());
        assertEquals(5, ana.puntuacion());
    }

    @Test
    void robarPasaLasCartasDelMismoValorAlJugadorActual() {
        crearConMazo(5, 5, 9);
        servicio.sacarCarta(1L, "Ana");
        servicio.terminarTurno(1L, "Ana");
        servicio.sacarCarta(1L, "Beto");

        PartidaDto partida = servicio.robarCartas(1L, "Beto", "Ana");

        assertEquals(Map.of(5, 2), partida.jugadores().get(1).cartasDelante());
        assertTrue(partida.jugadores().get(0).cartasDelante().isEmpty());
    }

    @Test
    void pedirElResultadoDeUnaPartidaEnCursoLanzaExcepcion() {
        crearConMazo(5, 9);

        assertThrows(IllegalStateException.class, () -> servicio.resultado(1L));
    }

    @Test
    void alTerminarLaPartidaSeGuardaUnaSolaVezEnElHistorial() {
        crearConMazo(7);
        servicio.sacarCarta(1L, "Ana");

        PartidaDto resultado = servicio.resultado(1L);
        assertThrows(IllegalStateException.class, () -> servicio.sacarCarta(1L, "Ana"));

        assertTrue(resultado.terminada());
        assertEquals(List.of("Ana"), resultado.ganadores());
        ArgumentCaptor<PartidaEntity> captor = ArgumentCaptor.forClass(PartidaEntity.class);
        verify(repositorio, times(1)).save(captor.capture());
        PartidaEntity guardada = captor.getValue();
        assertEquals(2, guardada.getJugadores().size());
        ResultadoJugadorEntity ana = guardada.getJugadores().get(0);
        assertEquals("Ana", ana.getNombre());
        assertEquals("7", ana.getCartas());
        assertEquals(7, ana.getPuntuacion());
        assertTrue(ana.isGanador());
        ResultadoJugadorEntity beto = guardada.getJugadores().get(1);
        assertEquals("Beto", beto.getNombre());
        assertEquals("", beto.getCartas());
        assertEquals(0, beto.getPuntuacion());
        assertFalse(beto.isGanador());
    }

    @Test
    void historialConvierteLasEntidadesGuardadasADtos() {
        PartidaEntity entidad = new PartidaEntity(LocalDateTime.of(2026, 10, 27, 15, 30));
        entidad.agregarJugador(new ResultadoJugadorEntity("Ana", "5,5,2", 12, true));
        entidad.agregarJugador(new ResultadoJugadorEntity("Beto", "9", 9, false));
        when(repositorio.findAllByOrderByFechaDesc()).thenReturn(List.of(entidad));

        List<HistorialDto> historial = servicio.historial();

        assertEquals(1, historial.size());
        assertEquals("2026-10-27T15:30", historial.get(0).fecha());
        assertEquals(2, historial.get(0).jugadores().size());
        JugadorHistorialDto ana = historial.get(0).jugadores().get(0);
        assertEquals("Ana", ana.nombre());
        assertEquals("5,5,2", ana.cartas());
        assertEquals(12, ana.puntuacion());
        assertTrue(ana.ganador());
    }

    @Test
    void cadaPartidaCreadaRecibeUnIdDistinto() {
        PartidaDto primera = crearConMazo(1, 2);
        PartidaDto segunda = crearConMazo(3, 4);

        assertEquals(1L, primera.id());
        assertEquals(2L, segunda.id());
        assertEquals(2L, servicio.consultar(2L).id());
        assertEquals("Ana", servicio.consultar(1L).turnoDe());
    }

    @Test
    void sacarLaPelusaEnvenenadaPierdeElTurnoYDescartaLasCartasDelante() {
        crearConMazo(5, 0, 9);
        servicio.sacarCarta(1L, "Ana");

        SacarCartaDto respuesta = servicio.sacarCarta(1L, "Ana");

        assertEquals("PELUSA_ENVENENADA", respuesta.resultado());
        assertEquals("Beto", respuesta.partida().turnoDe());
        assertTrue(respuesta.partida().jugadores().get(0).cartasDelante().isEmpty());
        assertEquals(0, respuesta.partida().jugadores().get(0).puntuacion());
    }
}