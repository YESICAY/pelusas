package com.pelusas.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record RobarRequest(@NotBlank String jugador, @NotBlank String victima) {
}