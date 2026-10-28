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

    @Test
    void creaUnMazoEstandarDe110Cartas() {
        Mazo mazo = new MazoAleatorioFactory().crear();

        assertEquals(110, mazo.cantidadRestante());
    }

    @Test
    void elMazoCreadoVienemezclado() {
        List<Integer> mezclado = valores(new MazoAleatorioFactory().crear());
        List<Integer> ordenado = valores(Mazo.estandar());

        assertNotEquals(ordenado, mezclado);
    }
}