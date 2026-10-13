package com.pelusas.backend.domain;

import java.util.Map;
import java.util.TreeMap;

public class Jugador {

    public static final int CARTAS_PARA_PERDER_TURNO = 3;

    private final String nombre;
    private final Map<Integer, Integer> cartasDelante = new TreeMap<>();

    public Jugador(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacio");
        }
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }

    public int cantidadDelante(int valor) {
        return cartasDelante.getOrDefault(valor, 0);
    }

    public int totalCartasDelante() {
        int total = 0;
        for (int cantidad : cartasDelante.values()) {
            total += cantidad;
        }
        return total;
    }

    public boolean provocaPerdida(Carta carta) {
        return cantidadDelante(carta.valor()) > 0
                && totalCartasDelante() >= CARTAS_PARA_PERDER_TURNO;
    }

    public void recibirCarta(Carta carta) {
        cartasDelante.merge(carta.valor(), 1, Integer::sum);
    }

    public void descartarCartasDelante() {
        cartasDelante.clear();
    }

    public Map<Integer, Integer> cartasDelante() {
        return new TreeMap<>(cartasDelante);
    }
}
