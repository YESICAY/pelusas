package com.pelusas.backend.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Jugador {

    public static final int CARTAS_PARA_PERDER_TURNO = 3;

    private final String nombre;
    private final Map<Integer, Integer> cartasDelante = new TreeMap<>();
    private final List<Carta> montonPuntos = new ArrayList<>();

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

    public void guardarCartasDelante() {
        for (Map.Entry<Integer, Integer> grupo : cartasDelante.entrySet()) {
            for (int i = 0; i < grupo.getValue(); i++) {
                montonPuntos.add(new Carta(grupo.getKey()));
            }
        }
        cartasDelante.clear();
    }

    public int puntuacion() {
        int total = 0;
        for (Carta carta : montonPuntos) {
            total += carta.valor();
        }
        return total;
    }

    public int cantidadEnMonton(int valor) {
        int cantidad = 0;
        for (Carta carta : montonPuntos) {
            if (carta.valor() == valor) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public List<Carta> montonPuntos() {
        return List.copyOf(montonPuntos);
    }

    public int quitarCartasDelante(int valor) {
        Integer cantidad = cartasDelante.remove(valor);
        return cantidad == null ? 0 : cantidad;
    }

    public int robarCartasDe(Jugador otro, int valor) {
        if (otro == this) {
            throw new IllegalArgumentException("Un jugador no puede robarse a si mismo");
        }
        int cantidad = otro.quitarCartasDelante(valor);
        for (int i = 0; i < cantidad; i++) {
            recibirCarta(new Carta(valor));
        }
        return cantidad;
    }
}