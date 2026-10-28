package com.pelusas.backend.service;

import com.pelusas.backend.domain.Mazo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MazoFijoFactoryTest {

    @Test
    void creaUnMazoConLaSecuenciaFijaDePruebas() {
        Mazo mazo = new MazoFijoFactory().crear();

        assertEquals(10, mazo.cantidadRestante());
        int[] esperados = {5, 5, 2, 5, 8, 8, 9, 9, 10, 10};
        for (int esperado : esperados) {
            assertEquals(esperado, mazo.sacarCarta().valor());
        }
        assertTrue(mazo.estaVacio());
    }

    @Test
    void cadaMazoCreadoEsIndependienteDelOtro() {
        MazoFijoFactory fabrica = new MazoFijoFactory();
        Mazo primero = fabrica.crear();
        Mazo segundo = fabrica.crear();

        primero.sacarCarta();

        assertEquals(9, primero.cantidadRestante());
        assertEquals(10, segundo.cantidadRestante());
    }
}