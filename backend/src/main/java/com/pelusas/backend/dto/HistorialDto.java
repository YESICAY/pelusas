package com.pelusas.backend.dto;

import java.util.List;

public record HistorialDto(Long id, String fecha, List<JugadorHistorialDto> jugadores) {
}