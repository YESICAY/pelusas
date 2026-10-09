package com.pelusas.backend.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartaTest {

    @Test
    void creaCartaConValorMinimoPermitido(){
        Carta carta = new Carta(1);

        assertEquals(1, carta.valor());
    }

    @Test
    void creaCartaConValorMaximoPermitido(){
        Carta carta = new Carta(10);

        assertEquals(10, carta.valor());
    }

    @Test
    void rechazaValorCero(){
        assertThrows(IllegalArgumentException.class, () -> new Carta(0));
    }

    @Test
    void rechazaValorOnce(){
        assertThrows(IllegalArgumentException.class, () -> new Carta(11));
    }

    @Test
    void rechazaValorNegativo(){
        assertThrows(IllegalArgumentException.class, () -> new Carta(-3));
    }

    @Test
    void dosCartasMismoValorSonIguales(){
        assertEquals(new Carta(5), new Carta(5));
    }

}