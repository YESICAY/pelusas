package com.pelusas.backend.support;

import com.pelusas.backend.domain.Carta;
import com.pelusas.backend.domain.Mazo;
import com.pelusas.backend.service.MazoFactory;

import java.util.ArrayList;
import java.util.List;

public class MazoDePruebaFactory implements MazoFactory {

    private List<Carta> cartas = null;

    public void definir(int... valores) {
        List<Carta> nuevas = new ArrayList<>();
        for (int valor : valores) {
            nuevas.add(valor == 0 ? Carta.pelusaEnvenenada() : new Carta(valor));
        }
        this.cartas = nuevas;
    }

    public void reiniciar() {
        this.cartas = null;
    }

    @Override
    public Mazo crear() {
        if (cartas == null) {
            return Mazo.estandar();
        }
        return new Mazo(cartas);
    }
}