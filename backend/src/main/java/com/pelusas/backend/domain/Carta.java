package com.pelusas.backend.domain;

public record Carta(int valor) {

    public static final int VALOR_MINIMO = 1;
    public static final int VALOR_MAXIMO = 10;

    public Carta{
        if (valor <  VALOR_MINIMO || valor > VALOR_MAXIMO){
            throw new IllegalArgumentException(
                    "Valor de la carta debe estar entre " + VALOR_MINIMO
                    + " y " + VALOR_MAXIMO + ", pero fue: " + valor);
        }
    }
}


