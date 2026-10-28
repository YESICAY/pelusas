package com.pelusas.backend.exception;

public class PartidaNoEncontradaException extends RuntimeException {

    public PartidaNoEncontradaException(long id) {
        super("No existe la partida " + id);
    }
}