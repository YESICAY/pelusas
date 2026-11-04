package com.pelusas.backend.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.ToIntFunction;

public class Partida {

    public static final int MINIMO_JUGADORES = 2;

    public enum ResultadoSacar { CONTINUA, TURNO_PERDIDO, PELUSA_ENVENENADA, PARTIDA_TERMINADA }

    private final List<Jugador> jugadores = new ArrayList<>();
    private final Mazo mazo;
    private int indiceTurno = 0;
    private int cartasSacadasEnTurno = 0;
    private Carta ultimaCarta = null;
    private Carta ultimaCartaSacada = null;
    private boolean terminada = false;
    private List<Jugador> ganadores = List.of();

    public Partida(List<String> nombres, Mazo mazo) {
        if (nombres.size() < MINIMO_JUGADORES) {
            throw new IllegalArgumentException("Se necesitan al menos " + MINIMO_JUGADORES + " jugadores");
        }
        if (new HashSet<>(nombres).size() != nombres.size()) {
            throw new IllegalArgumentException("Los nombres de los jugadores no pueden repetirse");
        }
        for (String nombre : nombres) {
            jugadores.add(new Jugador(nombre));
        }
        this.mazo = mazo;
    }

    public Jugador jugadorActual() {
        return jugadores.get(indiceTurno);
    }

    public List<Jugador> jugadores() {return List.copyOf(jugadores);}

    public boolean estaTerminada() {
        return terminada;
    }

    public List<Jugador> ganadores() {
        return List.copyOf(ganadores);
    }

    public int cartasRestantesEnMazo() {
        return mazo.cantidadRestante();
    }

    public Carta ultimaCartaSacada() {return ultimaCartaSacada;}

    public void guardarCartas() {
        validarEnCurso();
        if (cartasSacadasEnTurno > 0) {
            throw new IllegalStateException("Solo se pueden guardar cartas antes de sacar en el turno");
        }
        jugadorActual().guardarCartasDelante();
    }

    public ResultadoSacar sacarCarta() {
        validarEnCurso();
        Jugador actual = jugadorActual();
        Carta carta = mazo.sacarCarta();
        ultimaCartaSacada = carta;
        ResultadoSacar resultado;
        if (carta.envenenada()) {
            actual.descartarCartasDelante();
            pasarTurno();
            resultado = ResultadoSacar.PELUSA_ENVENENADA;
        } else if (actual.provocaPerdida(carta)) {
            actual.descartarCartasDelante();
            pasarTurno();
            resultado = ResultadoSacar.TURNO_PERDIDO;
        } else {
            actual.recibirCarta(carta);
            cartasSacadasEnTurno++;
            ultimaCarta = carta;
            resultado = ResultadoSacar.CONTINUA;
        }
        if (mazo.estaVacio()) {
            terminar();
            return ResultadoSacar.PARTIDA_TERMINADA;
        }
        return resultado;
    }

    public int robarCartas(String nombreVictima) {
        validarEnCurso();
        if (ultimaCarta == null) {
            throw new IllegalStateException("Solo se puede robar despues de sacar una carta en el turno");
        }
        Jugador victima = buscarJugador(nombreVictima);
        int robadas = jugadorActual().robarCartasDe(victima, ultimaCarta.valor());
        if (robadas == 0) {
            throw new IllegalStateException("El jugador no tiene cartas de ese valor para robar");
        }
        return robadas;
    }

    public void finalizarTurno() {
        validarEnCurso();
        if (cartasSacadasEnTurno == 0) {
            throw new IllegalStateException("Debes sacar al menos una carta antes de terminar el turno");
        }
        pasarTurno();
    }

    private void validarEnCurso() {
        if (terminada) {
            throw new IllegalStateException("La partida ya termino");
        }
    }

    private void pasarTurno() {
        indiceTurno = (indiceTurno + 1) % jugadores.size();
        cartasSacadasEnTurno = 0;
        ultimaCarta = null;
    }

    private Jugador buscarJugador(String nombre) {
        for (Jugador jugador : jugadores) {
            if (jugador.nombre().equals(nombre)) {
                return jugador;
            }
        }
        throw new IllegalArgumentException("No existe el jugador: " + nombre);
    }

    private void terminar() {
        terminada = true;
        for (Jugador jugador : jugadores) {
            jugador.guardarCartasDelante();
        }
        ganadores = calcularGanadores();
    }

    private List<Jugador> calcularGanadores() {
        return desempatar(conMejorValor(jugadores, Jugador::puntuacion));
    }

    static List<Jugador> desempatar(List<Jugador> empatados) {
        List<Jugador> candidatos = empatados;
        for (int valor = Carta.VALOR_MINIMO; valor <= Carta.VALOR_MAXIMO; valor++) {
            int valorActual = valor;
            candidatos = conMejorValor(candidatos, jugador -> jugador.cantidadEnMonton(valorActual));
        }
        return candidatos;
    }

    private static List<Jugador> conMejorValor(List<Jugador> lista, ToIntFunction<Jugador> criterio) {
        int mejor = Integer.MIN_VALUE;
        for (Jugador jugador : lista) {
            mejor = Math.max(mejor, criterio.applyAsInt(jugador));
        }
        List<Jugador> resultado = new ArrayList<>();
        for (Jugador jugador : lista) {
            if (criterio.applyAsInt(jugador) == mejor) {
                resultado.add(jugador);
            }
        }
        return resultado;
    }
}