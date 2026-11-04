package com.pelusas.backend.domain;

public record Carta(int valor, boolean envenenada) {

    public static final int VALOR_MINIMO = 1;
    public static final int VALOR_MAXIMO = 10;
    public static final int VALOR_ENVENENADA = 0;

    public Carta {
        if (envenenada) {
            if (valor != VALOR_ENVENENADA) {
                throw new IllegalArgumentException("La pelusa envenenada no tiene valor");
            }
        } else if(valor <  VALOR_MINIMO || valor > VALOR_MAXIMO){
            throw new IllegalArgumentException(
                    "Valor de la carta debe estar entre " + VALOR_MINIMO
                    + " y " + VALOR_MAXIMO + ", pero fue: " + valor);
        }
    }

    public Carta(int valor) {
        this(valor, false);
    }

    public static Carta pelusaEnvenenada() {
        return new Carta(VALOR_ENVENENADA, true);
    }
}


