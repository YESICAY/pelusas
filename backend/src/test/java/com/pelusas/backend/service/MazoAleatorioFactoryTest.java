package com.pelusas.backend.service;

import com.pelusas.backend.domain.Mazo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MazoAleatorioFactoryTest {

    private static List<Integer> valores(Mazo mazo) {
        List<Integer> valores = new ArrayList<>();
        while (!mazo.estaVacio()) {
            valores.add(mazo.sacarCarta().valor());
        }
        return valores;
    }

    private static int contarEnvenenadas(Mazo mazo) {
        int total = 0;
        while (!mazo.estaVacio()) {
            if (mazo.sacarCarta().envenenada()) {
                total++;
            }
        }
        return total;
    }

    @Test
    void conUnaEnvenenadaElMazoTiene111Cartas() {
        Mazo mazo = new MazoAleatorioFactory(1).crear();

        assertEquals(111, mazo.cantidadRestante());
        assertEquals(1, contarEnvenenadas(mazo));
    }

    @Test
    void sinEnvenenadasElMazoTiene110Cartas() {
        Mazo mazo = new MazoAleatorioFactory(0).crear();

        assertEquals(110, mazo.cantidadRestante());
        assertEquals(0, contarEnvenenadas(mazo));
    }

    @Test
    void laCantidadDeEnvenenadasEsConfigurable() {
        assertEquals(3, contarEnvenenadas(new MazoAleatorioFactory(3).crear()));
    }

    @Test
    void elMazoCreadoVieneMezclado() {
        List<Integer> mezclado = valores(new MazoAleatorioFactory(0).crear());
        List<Integer> ordenado = valores(Mazo.estandar());

        assertNotEquals(ordenado, mezclado);
    }
}