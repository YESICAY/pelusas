package com.pelusas.backend.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MazoTest {

    private static List<Integer> extraerValores(Mazo mazo){
        List<Integer> valores = new ArrayList<>();
        while (!mazo.estaVacio()){
            valores.add(mazo.sacarCarta().valor());
        }
        return valores;
    }

    private static int[] contarPorValor(List<Integer> valores){
        int[] conteo = new int[Carta.VALOR_MAXIMO +1];
        for (int valor : valores){
            conteo[valor]++;
        }
        return conteo;
    }

    @Test
    void mazoEstandarTiene110Cartas(){
        Mazo mazo = Mazo.estandar();

        assertEquals(110, mazo.cantidadRestante());
    }

    @Test
    void mazoEstandarTiene13CartasDeCadaValorDel1Al5() {
        int[] conteo = contarPorValor(extraerValores(Mazo.estandar()));

        for (int valor = 1; valor <= 5; valor++) {
            assertEquals(13, conteo[valor], "La cntidad es incorrecta para el valor " + valor);
        }
    }

    @Test
    void mazoEstandarTiene9CartasDeCadaValorDel6Al10() {
        int[] conteo = contarPorValor(extraerValores(Mazo.estandar()));

        for (int valor = 6; valor <= 10; valor++) {
            assertEquals(9, conteo[valor], "La cntidad es incorrecta para el valor " + valor);
        }
    }

    @Test
    void sacarCartaDevuelveLaPrimeraYReduceElMazoEnUna() {
        Mazo mazo = new Mazo(List.of(new Carta(3), new Carta(7)));

        Carta carta = mazo.sacarCarta();

        assertEquals(new Carta(3), carta);
        assertEquals(1, mazo.cantidadRestante());
        assertEquals(new Carta(7), mazo.sacarCarta());
    }

    @Test
    void sacarCartaDeMazoVacioLanzaExcepcion() {
        Mazo mazo = new Mazo(List.of());

        assertThrows(IllegalStateException.class, mazo::sacarCarta);
    }

    @Test
    void estaVacioEsFalsoConCartasYVerdaderoSinCartas() {
        Mazo mazo = new Mazo(List.of(new Carta(1)));

        assertFalse(mazo.estaVacio());

        mazo.sacarCarta();

        assertTrue(mazo.estaVacio());
    }

    @Test
    void elMazoNoDependeDeLaListaOriginal() {
        List<Carta> original = new ArrayList<>(List.of(new Carta(2), new Carta(4)));
        Mazo mazo = new Mazo(original);

        original.clear();

        assertEquals(2, mazo.cantidadRestante());
    }

    @Test
    void mezclarConservaLas110CartasYSuDistribucion() {
        Mazo mazo = Mazo.estandar();

        mazo.mezclar(new Random(42));

        assertEquals(110, mazo.cantidadRestante());
        int[] conteo = contarPorValor(extraerValores(mazo));
        assertEquals(13, conteo[1]);
        assertEquals(13, conteo[5]);
        assertEquals(9, conteo[6]);
        assertEquals(9, conteo[10]);
    }

    @Test
    void mezclarConLaMismaSemillaProduceElMismoOrden() {
        Mazo mazoA = Mazo.estandar();
        Mazo mazoB = Mazo.estandar();

        mazoA.mezclar(new Random(7));
        mazoB.mezclar(new Random(7));

        assertEquals(extraerValores(mazoA), extraerValores(mazoB));
    }

    @Test
    void mezclarCambiaElOrdenOriginal() {
        Mazo ordenado = Mazo.estandar();
        Mazo mezclado = Mazo.estandar();

        mezclado.mezclar(new Random(42));

        assertNotEquals(extraerValores(ordenado), extraerValores(mezclado));
    }

    @Test
    void elMazoConEnvenenadasSumaEsasCartasALas110() {
        Mazo mazo = Mazo.estandarConEnvenenadas(2);

        assertEquals(112, mazo.cantidadRestante());
        int envenenadas = 0;
        int[] conteo = new int[Carta.VALOR_MAXIMO + 1];
        while (!mazo.estaVacio()) {
            Carta carta = mazo.sacarCarta();
            if (carta.envenenada()) {
                envenenadas++;
            } else {
                conteo[carta.valor()]++;
            }
        }
        assertEquals(2, envenenadas);
        assertEquals(13, conteo[1]);
        assertEquals(13, conteo[5]);
        assertEquals(9, conteo[6]);
        assertEquals(9, conteo[10]);
    }

    @Test
    void sinEnvenenadasElMazoEsElEstandarDe110() {
        assertEquals(110, Mazo.estandarConEnvenenadas(0).cantidadRestante());
    }

    @Test
    void laCantidadDeEnvenenadasNoPuedeSerNegativa() {
        assertThrows(IllegalArgumentException.class, () -> Mazo.estandarConEnvenenadas(-1));
    }
}