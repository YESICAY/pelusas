package com.pelusas.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resultados_jugador")
public class ResultadoJugadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "partida_id")
    private PartidaEntity partida;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String cartas;

    @Column(nullable = false)
    private int puntuacion;

    @Column(nullable = false)
    private boolean ganador;

    protected ResultadoJugadorEntity() {
    }

    public ResultadoJugadorEntity(String nombre, String cartas, int puntuacion, boolean ganador) {
        this.nombre = nombre;
        this.cartas = cartas;
        this.puntuacion = puntuacion;
        this.ganador = ganador;
    }

    void asignarPartida(PartidaEntity partida) {
        this.partida = partida;
    }

    public PartidaEntity getPartida() {
        return partida;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCartas() {
        return cartas;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public boolean isGanador() {
        return ganador;
    }
}