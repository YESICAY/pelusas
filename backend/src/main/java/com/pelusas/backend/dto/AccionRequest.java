package com.pelusas.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AccionRequest(@NotBlank String jugador) {
}