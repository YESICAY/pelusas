package com.pelusas.backend.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DesempateTest {

    private static Jugador conMonton(String nombre, int... valores) {
        Jugador jugador = new Jugador(nombre);
        for (int valor : valores) {
            jugador.recibirCarta(new Carta(valor));
        }
        jugador.guardarCartasDelante();
        return jugador;
    }

    @Test
    void ganaQuienTieneMasCartasDeValor1() {
        Jugador ana = conMonton("Ana", 1, 1, 4);
        Jugador beto = conMonton("Beto", 1, 5);

        assertEquals(List.of(ana), Partida.desempatar(List.of(ana, beto)));
    }

    @Test
    void siEmpatanEnValor1DecideElValor2() {
        Jugador ana = conMonton("Ana", 1, 2, 2);
        Jugador beto = conMonton("Beto", 1, 3, 3);

        assertEquals(List.of(ana), Partida.desempatar(List.of(ana, beto)));
    }

    @Test
    void elValor10SeComparaAlFinal() {
        Jugador ana = conMonton("Ana", 10);
        Jugador beto = conMonton("Beto");

        assertEquals(List.of(ana), Partida.desempatar(List.of(ana, beto)));
    }

    @Test
    void losValoresMasBajosTienenPrioridad() {
        Jugador ana = conMonton("Ana", 1, 10, 10);
        Jugador beto = conMonton("Beto", 1, 1);

        assertEquals(List.of(beto), Partida.desempatar(List.of(ana, beto)));
    }

    @Test
    void unEmpateDeTresSeReduceProgresivamente() {
        Jugador ana = conMonton("Ana", 1, 1, 3);
        Jugador beto = conMonton("Beto", 1, 1, 2);
        Jugador carla = conMonton("Carla", 1, 4);

        assertEquals(List.of(beto), Partida.desempatar(List.of(ana, beto, carla)));
    }

    @Test
    void siEmpatanEnTodosLosValoresSeMantienenTodos() {
        Jugador ana = conMonton("Ana", 5);
        Jugador beto = conMonton("Beto", 5);

        assertEquals(List.of(ana, beto), Partida.desempatar(List.of(ana, beto)));
    }
}