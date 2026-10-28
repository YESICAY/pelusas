package com.pelusas.backend.dto;

import com.pelusas.backend.domain.Jugador;
import com.pelusas.backend.domain.Partida;
import com.pelusas.backend.entity.PartidaEntity;
import com.pelusas.backend.entity.ResultadoJugadorEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public final class PartidaMapper {

    private PartidaMapper() {
    }

    public static PartidaDto partida(long id, Partida partida) {
        List<JugadorDto> jugadores = partida.jugadores().stream()
                .map(PartidaMapper::jugador)
                .toList();
        List<String> ganadores = partida.ganadores().stream()
                .map(Jugador::nombre)
                .toList();
        String turnoDe = partida.estaTerminada() ? null : partida.jugadorActual().nombre();
        return new PartidaDto(id, partida.estaTerminada(), turnoDe,
                partida.cartasRestantesEnMazo(), jugadores, ganadores);
    }

    public static JugadorDto jugador(Jugador jugador) {
        return new JugadorDto(jugador.nombre(), jugador.cartasDelante(),
                jugador.montonPuntos().size(), jugador.puntuacion());
    }

    public static PartidaEntity entidad(Partida partida, LocalDateTime fecha) {
        PartidaEntity entidad = new PartidaEntity(fecha);
        for (Jugador jugador : partida.jugadores()) {
            String cartas = jugador.montonPuntos().stream()
                    .map(carta -> String.valueOf(carta.valor()))
                    .collect(Collectors.joining(","));
            entidad.agregarJugador(new ResultadoJugadorEntity(jugador.nombre(), cartas,
                    jugador.puntuacion(), partida.ganadores().contains(jugador)));
        }
        return entidad;
    }

    public static HistorialDto historial(PartidaEntity entidad) {
        List<JugadorHistorialDto> jugadores = entidad.getJugadores().stream()
                .map(r -> new JugadorHistorialDto(r.getNombre(), r.getCartas(),
                        r.getPuntuacion(), r.isGanador()))
                .toList();
        return new HistorialDto(entidad.getId(), entidad.getFecha().toString(), jugadores);
    }
}