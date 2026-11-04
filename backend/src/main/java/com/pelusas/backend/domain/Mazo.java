package com.pelusas.backend.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Mazo {

    private static final int ULTIMO_VALOR_CON_13_CARTAS = 5;
    private static final int CARTAS_POR_VALOR_BAJO = 13;
    private static final int CARTAS_POR_VALOR_ALTO = 9;

    private final List<Carta> cartas;

    public Mazo(List<Carta> cartas){
        this.cartas = new ArrayList<>(cartas);
    }

    public static Mazo estandar(){
        List<Carta> cartas = new ArrayList<>();
        for (int valor = Carta.VALOR_MINIMO; valor <= Carta.VALOR_MAXIMO; valor++){
            int cantidad = valor <= ULTIMO_VALOR_CON_13_CARTAS
                    ? CARTAS_POR_VALOR_BAJO
                    : CARTAS_POR_VALOR_ALTO;
            for (int i = 0; i < cantidad; i++){
                cartas.add(new Carta(valor));
            }
        }
        return new Mazo(cartas);
    }

    public static Mazo estandarConEnvenenadas(int cantidadEnvenenadas) {
        if (cantidadEnvenenadas < 0) {
            throw new IllegalArgumentException("La cantidad de pelusas envenenadas no puede ser negativa");
        }
        Mazo mazo = estandar();
        for (int i = 0; i < cantidadEnvenenadas; i++) {
            mazo.cartas.add(Carta.pelusaEnvenenada());
        }
        return mazo;
    }

    public void mezclar(Random random){
        Collections.shuffle(cartas, random);
    }

    public Carta sacarCarta(){
        if (cartas.isEmpty()){
            throw  new IllegalStateException("No hay más cartas en el mazo");
        }
        return cartas.remove(0);
    }

    public boolean estaVacio(){
        return cartas.isEmpty();
    }

    public int cantidadRestante(){
        return cartas.size();
    }
}
