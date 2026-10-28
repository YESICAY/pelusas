package com.pelusas.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CrearPartidaRequest(@NotNull @Size(min = 2) List<@NotBlank String> jugadores) {
}