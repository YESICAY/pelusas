package com.pelusas.backend.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JugadorTest {

    private static Jugador jugadorConCartas(int... valores) {
        Jugador jugador = new Jugador("Ana");
        for (int valor : valores) {
            jugador.recibirCarta(new Carta(valor));
        }
        return jugador;
    }

    @Test
    void nombreNuloVacioOSoloEspaciosEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> new Jugador(null));
        assertThrows(IllegalArgumentException.class, () -> new Jugador(""));
        assertThrows(IllegalArgumentException.class, () -> new Jugador("   "));
    }

    @Test
    void jugadorNuevoTieneNombreYNoTieneCartasDelante() {
        Jugador jugador = new Jugador("Ana");

        assertEquals("Ana", jugador.nombre());
        assertEquals(0, jugador.totalCartasDelante());
    }

    @Test
    void recibirCartasLasAgrupaPorValor() {
        Jugador jugador = jugadorConCartas(5, 5, 2);

        assertEquals(2, jugador.cantidadDelante(5));
        assertEquals(1, jugador.cantidadDelante(2));
        assertEquals(0, jugador.cantidadDelante(7));
        assertEquals(3, jugador.totalCartasDelante());
    }

    @Test
    void cartasDelanteSeMuestranAgrupadasPorValor() {
        Jugador jugador = jugadorConCartas(5, 2, 5);

        assertEquals(Map.of(2, 1, 5, 2), jugador.cartasDelante());
    }

    @Test
    void cartasDelanteDevuelveUnaCopiaQueNoAlteraAlJugador() {
        Jugador jugador = jugadorConCartas(5);

        jugador.cartasDelante().clear();

        assertEquals(1, jugador.totalCartasDelante());
    }

    @Test
    void sinCartasDelanteNoPierde() {
        Jugador jugador = new Jugador("Ana");

        assertFalse(jugador.provocaPerdida(new Carta(5)));
    }

    @Test
    void repetirConUnaSolaCartaDelanteNoPierde() {
        Jugador jugador = jugadorConCartas(5);

        assertFalse(jugador.provocaPerdida(new Carta(5)));
    }

    @Test
    void repetirConDosCartasDelanteNoPierde() {
        assertFalse(jugadorConCartas(5, 5).provocaPerdida(new Carta(5)));
        assertFalse(jugadorConCartas(5, 2).provocaPerdida(new Carta(5)));
    }

    @Test
    void repetirConExactamenteTresCartasDelantePierde() {
        Jugador jugador = jugadorConCartas(5, 2, 3);

        assertTrue(jugador.provocaPerdida(new Carta(5)));
    }

    @Test
    void repetirConMasDeTresCartasDelantePierde() {
        Jugador jugador = jugadorConCartas(5, 5, 2, 3);

        assertTrue(jugador.provocaPerdida(new Carta(5)));
    }

    @Test
    void cartaNoRepetidaNoPierdeAunqueTengaTresOMasCartas() {
        assertFalse(jugadorConCartas(1, 2, 3).provocaPerdida(new Carta(4)));
        assertFalse(jugadorConCartas(1, 2, 3, 4, 5).provocaPerdida(new Carta(6)));
    }

    @Test
    void descartarVaciaLasCartasDelante() {
        Jugador jugador = jugadorConCartas(5, 5, 2);

        jugador.descartarCartasDelante();

        assertEquals(0, jugador.totalCartasDelante());
        assertEquals(0, jugador.cantidadDelante(5));
    }

}