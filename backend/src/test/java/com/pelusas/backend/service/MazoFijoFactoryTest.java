package com.pelusas.backend.service;

import com.pelusas.backend.domain.Carta;
import com.pelusas.backend.domain.Mazo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MazoFijoFactoryTest {

    @Test
    void creaUnMazoConLaSecuenciaIndicada() {
        Mazo mazo = new MazoFijoFactory("5, 5,2,5,8,8,9,9,10,10").crear();

        assertEquals(10, mazo.cantidadRestante());
        int[] esperados = {5, 5, 2, 5, 8, 8, 9, 9, 10, 10};
        for (int esperado : esperados) {
            assertEquals(new Carta(esperado), mazo.sacarCarta());
        }
        assertTrue(mazo.estaVacio());
    }

    @Test
    void elValorCeroSeConvierteEnPelusaEnvenenada() {
        Mazo mazo = new MazoFijoFactory("5,0").crear();

        assertEquals(new Carta(5), mazo.sacarCarta());
        assertTrue(mazo.sacarCarta().envenenada());
    }

    @Test
    void cadaMazoCreadoEsIndependienteDelOtro() {
        MazoFijoFactory fabrica = new MazoFijoFactory("5,2");
        Mazo primero = fabrica.crear();
        Mazo segundo = fabrica.crear();

        primero.sacarCarta();

        assertEquals(1, primero.cantidadRestante());
        assertEquals(2, segundo.cantidadRestante());
    }
}