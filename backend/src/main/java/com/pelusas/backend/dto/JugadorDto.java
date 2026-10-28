package com.pelusas.backend.dto;

import java.util.Map;

public record JugadorDto(String nombre, Map<Integer, Integer> cartasDelante,
                         int cartasEnMonton, int puntuacion) {
}