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

    @Test
    void jugadorNuevoTienePuntuacionCeroYMontonVacio() {
        Jugador jugador = new Jugador("Ana");

        assertEquals(0, jugador.puntuacion());
        assertTrue(jugador.montonPuntos().isEmpty());
    }

    @Test
    void guardarCartasDelanteLasMueveAlMontonYLasQuitaDeDelante() {
        Jugador jugador = jugadorConCartas(5, 5, 2);

        jugador.guardarCartasDelante();

        assertEquals(0, jugador.totalCartasDelante());
        assertEquals(3, jugador.montonPuntos().size());
        assertEquals(12, jugador.puntuacion());
    }

    @Test
    void guardarSinCartasDelanteNoCambiaNada() {
        Jugador jugador = new Jugador("Ana");

        jugador.guardarCartasDelante();

        assertEquals(0, jugador.puntuacion());
        assertTrue(jugador.montonPuntos().isEmpty());
    }

    @Test
    void guardarVariasVecesAcumulaEnElMonton() {
        Jugador jugador = jugadorConCartas(5);
        jugador.guardarCartasDelante();
        jugador.recibirCarta(new Carta(3));

        jugador.guardarCartasDelante();

        assertEquals(2, jugador.montonPuntos().size());
        assertEquals(8, jugador.puntuacion());
    }

    @Test
    void descartarNoSumaPuntos() {
        Jugador jugador = jugadorConCartas(5, 5, 2);

        jugador.descartarCartasDelante();

        assertEquals(0, jugador.puntuacion());
        assertTrue(jugador.montonPuntos().isEmpty());
    }

    @Test
    void elMontonDevueltoNoSePuedeModificar() {
        Jugador jugador = new Jugador("Ana");

        assertThrows(UnsupportedOperationException.class,
                () -> jugador.montonPuntos().add(new Carta(1)));
    }

    @Test
    void quitarCartasDelanteDevuelveLaCantidadYQuitaSoloEseGrupo() {
        Jugador jugador = jugadorConCartas(5, 5, 2);

        int quitadas = jugador.quitarCartasDelante(5);

        assertEquals(2, quitadas);
        assertEquals(0, jugador.cantidadDelante(5));
        assertEquals(1, jugador.cantidadDelante(2));
    }

    @Test
    void quitarUnValorQueNoTieneDevuelveCero() {
        Jugador jugador = jugadorConCartas(5);

        assertEquals(0, jugador.quitarCartasDelante(9));
        assertEquals(1, jugador.totalCartasDelante());
    }

    @Test
    void robarCartasLasPasaDelanteDelJugadorActual() {
        Jugador ladron = jugadorConCartas(2);
        Jugador victima = jugadorConCartas(5, 5, 3);

        int robadas = ladron.robarCartasDe(victima, 5);

        assertEquals(2, robadas);
        assertEquals(2, ladron.cantidadDelante(5));
        assertEquals(3, ladron.totalCartasDelante());
        assertEquals(0, victima.cantidadDelante(5));
        assertEquals(1, victima.cantidadDelante(3));
    }

    @Test
    void robarUnValorQueElOtroNoTieneNoCambiaNada() {
        Jugador ladron = jugadorConCartas(2);
        Jugador victima = jugadorConCartas(3);

        int robadas = ladron.robarCartasDe(victima, 5);

        assertEquals(0, robadas);
        assertEquals(1, ladron.totalCartasDelante());
        assertEquals(1, victima.totalCartasDelante());
    }

    @Test
    void unJugadorNoPuedeRobarseASiMismo() {
        Jugador jugador = jugadorConCartas(5, 5);

        assertThrows(IllegalArgumentException.class, () -> jugador.robarCartasDe(jugador, 5));
    }

    @Test
    void cantidadEnMontonCuentaSoloLasCartasDeEseValor() {
        Jugador jugador = jugadorConCartas(5, 5, 2);
        jugador.guardarCartasDelante();

        assertEquals(2, jugador.cantidadEnMonton(5));
        assertEquals(1, jugador.cantidadEnMonton(2));
        assertEquals(0, jugador.cantidadEnMonton(9));
    }

    @Test
    void cantidadEnMontonNoCuentaLasCartasQueEstanDelante() {
        Jugador jugador = jugadorConCartas(5);

        assertEquals(0, jugador.cantidadEnMonton(5));
    }

    @Test
    void unaPelusaEnvenenadaNoPuedeColocarseDelante() {
        Jugador jugador = new Jugador("Ana");

        assertThrows(IllegalArgumentException.class,
                () -> jugador.recibirCarta(Carta.pelusaEnvenenada()));
        assertEquals(0, jugador.totalCartasDelante());
    }
}