package com.pelusas.backend.dto;

import java.util.List;

public record PartidaDto(long id, boolean terminada, String turnoDe, int cartasRestantesEnMazo,
                         List<JugadorDto> jugadores, List<String> ganadores) {
}