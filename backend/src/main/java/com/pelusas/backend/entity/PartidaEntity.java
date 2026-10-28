package com.pelusas.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partidas")
public class PartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<ResultadoJugadorEntity> jugadores = new ArrayList<>();

    protected PartidaEntity() {
    }

    public PartidaEntity(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void agregarJugador(ResultadoJugadorEntity jugador) {
        jugador.asignarPartida(this);
        jugadores.add(jugador);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public List<ResultadoJugadorEntity> getJugadores() {
        return List.copyOf(jugadores);
    }
}