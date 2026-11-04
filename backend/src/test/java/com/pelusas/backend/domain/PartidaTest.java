package com.pelusas.backend.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartidaTest {

    private static Mazo mazoDe(int... valores) {
        List<Carta> cartas = new ArrayList<>();
        for (int valor : valores) {
            cartas.add(valor == 0 ? Carta.pelusaEnvenenada() : new Carta(valor));
        }
        return new Mazo(cartas);
    }

    private static Partida partida(int... valores) {
        return new Partida(List.of("Ana", "Beto"), mazoDe(valores));
    }

    @Test
    void laPartidaNuevaEmpiezaConElPrimerJugador() {
        Partida partida = partida(1, 2);

        assertEquals("Ana", partida.jugadorActual().nombre());
        assertEquals(2, partida.jugadores().size());
        assertEquals(2, partida.cartasRestantesEnMazo());
        assertFalse(partida.estaTerminada());
        assertTrue(partida.ganadores().isEmpty());
    }

    @Test
    void rechazaMenosDeDosJugadores() {
        assertThrows(IllegalArgumentException.class,
                () -> new Partida(List.of("Ana"), mazoDe(1)));
    }

    @Test
    void rechazaNombresRepetidos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Partida(List.of("Ana", "Ana"), mazoDe(1)));
    }

    @Test
    void sacarCartaLaPoneDelanteDelJugadorActual() {
        Partida partida = partida(5, 9);

        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.CONTINUA, resultado);
        assertEquals(1, partida.jugadorActual().cantidadDelante(5));
        assertEquals("Ana", partida.jugadorActual().nombre());
        assertEquals(1, partida.cartasRestantesEnMazo());
    }

    @Test
    void sacarVariasCartasAcumulaEnElMismoTurno() {
        Partida partida = partida(5, 2, 9);

        partida.sacarCarta();
        partida.sacarCarta();

        assertEquals(2, partida.jugadorActual().totalCartasDelante());
        assertEquals("Ana", partida.jugadorActual().nombre());
    }

    @Test
    void repetirConMenosDeTresCartasNoPierde() {
        Partida partida = partida(5, 5, 9);

        partida.sacarCarta();
        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.CONTINUA, resultado);
        assertEquals(2, partida.jugadorActual().cantidadDelante(5));
    }

    @Test
    void repetirConExactamenteTresCartasPierdeElTurno() {
        Partida partida = partida(5, 2, 3, 5, 9);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();
        partida.sacarCarta();
        partida.sacarCarta();

        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.TURNO_PERDIDO, resultado);
        assertEquals(0, ana.totalCartasDelante());
        assertEquals(0, ana.puntuacion());
        assertEquals("Beto", partida.jugadorActual().nombre());
        assertFalse(partida.estaTerminada());
    }

    @Test
    void repetirConMasDeTresCartasPierdeElTurno() {
        Partida partida = partida(5, 5, 2, 3, 5, 9);
        Jugador ana = partida.jugadorActual();
        for (int i = 0; i < 4; i++) {
            partida.sacarCarta();
        }

        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.TURNO_PERDIDO, resultado);
        assertEquals(0, ana.totalCartasDelante());
        assertEquals("Beto", partida.jugadorActual().nombre());
    }

    @Test
    void cartaNoRepetidaNoPierdeAunqueTengaTresCartas() {
        Partida partida = partida(1, 2, 3, 4, 9);
        partida.sacarCarta();
        partida.sacarCarta();
        partida.sacarCarta();

        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.CONTINUA, resultado);
        assertEquals(4, partida.jugadorActual().totalCartasDelante());
    }

    @Test
    void finalizarTurnoPasaAlSiguienteYConservaLasCartasDelante() {
        Partida partida = partida(5, 9);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();

        partida.finalizarTurno();

        assertEquals("Beto", partida.jugadorActual().nombre());
        assertEquals(1, ana.cantidadDelante(5));
    }

    @Test
    void finalizarTurnoSinSacarLanzaExcepcion() {
        Partida partida = partida(5, 9);

        assertThrows(IllegalStateException.class, partida::finalizarTurno);
    }

    @Test
    void despuesDeDarLaVueltaTodosLosJugadoresElTurnoVuelveAlPrimero() {
        Partida partida = partida(5, 9, 2);
        partida.sacarCarta();
        partida.finalizarTurno();
        partida.sacarCarta();
        partida.finalizarTurno();

        assertEquals("Ana", partida.jugadorActual().nombre());
    }

    @Test
    void guardarCartasAntesDeSacarLasMueveAlMonton() {
        Partida partida = partida(5, 9, 2, 9);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();
        partida.finalizarTurno();
        partida.sacarCarta();
        partida.finalizarTurno();

        partida.guardarCartas();

        assertEquals(5, ana.puntuacion());
        assertEquals(0, ana.totalCartasDelante());
    }

    @Test
    void guardarCartasDespuesDeSacarLanzaExcepcion() {
        Partida partida = partida(5, 9);
        partida.sacarCarta();

        assertThrows(IllegalStateException.class, partida::guardarCartas);
    }

    @Test
    void robarPasaLasCartasDelMismoValorAlJugadorActual() {
        Partida partida = partida(5, 5, 9);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();
        partida.finalizarTurno();
        Jugador beto = partida.jugadorActual();
        partida.sacarCarta();

        int robadas = partida.robarCartas("Ana");

        assertEquals(1, robadas);
        assertEquals(2, beto.cantidadDelante(5));
        assertEquals(0, ana.cantidadDelante(5));
    }

    @Test
    void robarSinHaberSacadoLanzaExcepcion() {
        Partida partida = partida(5, 9);

        assertThrows(IllegalStateException.class, () -> partida.robarCartas("Beto"));
    }

    @Test
    void robarUnValorQueLaVictimaNoTieneLanzaExcepcion() {
        Partida partida = partida(5, 3, 9);
        partida.sacarCarta();
        partida.finalizarTurno();
        partida.sacarCarta();

        assertThrows(IllegalStateException.class, () -> partida.robarCartas("Ana"));
    }

    @Test
    void robarDeUnJugadorInexistenteLanzaExcepcion() {
        Partida partida = partida(5, 9);
        partida.sacarCarta();

        assertThrows(IllegalArgumentException.class, () -> partida.robarCartas("Zoe"));
    }

    @Test
    void unJugadorNoPuedeRobarseASiMismo() {
        Partida partida = partida(5, 9);
        partida.sacarCarta();

        assertThrows(IllegalArgumentException.class, () -> partida.robarCartas("Ana"));
    }

    @Test
    void sacarLaUltimaCartaTerminaLaPartidaYGuardaLasCartas() {
        Partida partida = partida(7);
        Jugador ana = partida.jugadorActual();

        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.PARTIDA_TERMINADA, resultado);
        assertTrue(partida.estaTerminada());
        assertEquals(7, ana.puntuacion());
        assertEquals(0, ana.totalCartasDelante());
        assertEquals(List.of(ana), partida.ganadores());
    }

    @Test
    void despuesDeTerminarNoSePuedeActuar() {
        Partida partida = partida(7);
        partida.sacarCarta();

        assertThrows(IllegalStateException.class, partida::sacarCarta);
        assertThrows(IllegalStateException.class, partida::guardarCartas);
        assertThrows(IllegalStateException.class, partida::finalizarTurno);
        assertThrows(IllegalStateException.class, () -> partida.robarCartas("Beto"));
    }

    @Test
    void ganaElJugadorConMayorPuntuacion() {
        Partida partida = partida(9, 2);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();
        partida.finalizarTurno();

        partida.sacarCarta();

        assertTrue(partida.estaTerminada());
        assertEquals(List.of(ana), partida.ganadores());
    }

    @Test
    void elEmpateSeDesempataPorMayorCantidadDeCartasDeValor1() {
        Partida partida = partida(1, 1, 2, 4);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();
        partida.sacarCarta();
        partida.sacarCarta();
        partida.finalizarTurno();

        partida.sacarCarta();

        assertEquals(4, ana.puntuacion());
        assertEquals(4, partida.jugadores().get(1).puntuacion());
        assertEquals(List.of(ana), partida.ganadores());
    }

    @Test
    void siEmpatanEnElValor1SeComparaElValor2() {
        Partida partida = partida(1, 2, 2, 1, 4);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();
        partida.sacarCarta();
        partida.sacarCarta();
        partida.finalizarTurno();
        partida.sacarCarta();

        partida.sacarCarta();

        assertEquals(5, ana.puntuacion());
        assertEquals(5, partida.jugadores().get(1).puntuacion());
        assertEquals(List.of(ana), partida.ganadores());
    }

    @Test
    void siSiguenEmpatadosComparteLaVictoria() {
        Partida partida = partida(7, 7);
        partida.sacarCarta();
        partida.finalizarTurno();

        partida.sacarCarta();

        assertEquals(2, partida.ganadores().size());
    }

    @Test
    void perderElTurnoConLaUltimaCartaTambienTerminaLaPartida() {
        Partida partida = partida(5, 2, 3, 5);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();
        partida.sacarCarta();
        partida.sacarCarta();

        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.PARTIDA_TERMINADA, resultado);
        assertEquals(0, ana.puntuacion());
        assertEquals(2, partida.ganadores().size());
    }

    @Test
    void sacarCartaRegistraLaUltimaCartaSacadaAunqueHagaPerderElTurno() {
        Partida partida = partida(5, 2, 3, 5, 9);

        partida.sacarCarta();
        assertEquals(new Carta(5), partida.ultimaCartaSacada());
        partida.sacarCarta();
        assertEquals(new Carta(2), partida.ultimaCartaSacada());
        partida.sacarCarta();
        assertEquals(new Carta(3), partida.ultimaCartaSacada());
        partida.sacarCarta();

        assertEquals(new Carta(5), partida.ultimaCartaSacada());
    }

    @Test
    void sacarLaPelusaEnvenenadaHacePerderElTurnoAunqueNoHayaCartasDelante() {
        Partida partida = partida(0, 9);

        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.PELUSA_ENVENENADA, resultado);
        assertEquals("Beto", partida.jugadorActual().nombre());
        assertFalse(partida.estaTerminada());
    }

    @Test
    void laPelusaEnvenenadaDescartaTodasLasCartasDelanteSinSumarPuntos() {
        Partida partida = partida(5, 2, 0, 9);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();
        partida.sacarCarta();

        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.PELUSA_ENVENENADA, resultado);
        assertEquals(0, ana.totalCartasDelante());
        assertEquals(0, ana.puntuacion());
        assertTrue(ana.montonPuntos().isEmpty());
        assertEquals("Beto", partida.jugadorActual().nombre());
    }

    @Test
    void laPelusaEnvenenadaNoAfectaAlMontonYaGuardado() {
        Partida partida = partida(5, 9, 2, 0, 9);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();
        partida.finalizarTurno();
        partida.sacarCarta();
        partida.finalizarTurno();
        partida.guardarCartas();
        partida.sacarCarta();

        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.PELUSA_ENVENENADA, resultado);
        assertEquals(0, ana.totalCartasDelante());
        assertEquals(5, ana.puntuacion());
        assertEquals(1, ana.montonPuntos().size());
    }

    @Test
    void despuesDeLaPelusaEnvenenadaNoSePuedeRobar() {
        Partida partida = partida(5, 0, 9);
        partida.sacarCarta();
        partida.finalizarTurno();
        partida.sacarCarta();

        assertThrows(IllegalStateException.class, () -> partida.robarCartas("Beto"));
    }

    @Test
    void laPelusaEnvenenadaComoUltimaCartaTerminaLaPartidaSinGuardarLoQueEraDeQuienLaSaco() {
        Partida partida = partida(5, 0);
        Jugador ana = partida.jugadorActual();
        partida.sacarCarta();
        partida.finalizarTurno();
        Jugador beto = partida.jugadorActual();

        Partida.ResultadoSacar resultado = partida.sacarCarta();

        assertEquals(Partida.ResultadoSacar.PARTIDA_TERMINADA, resultado);
        assertTrue(partida.estaTerminada());
        assertEquals(5, ana.puntuacion());
        assertEquals(0, beto.puntuacion());
        assertEquals(List.of(ana), partida.ganadores());
    }
}