package com.pelusas.backend.service;

import com.pelusas.backend.domain.Mazo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MazoAleatorioFactory implements MazoFactory {

    private final int cartasEnvenenadas;

    public MazoAleatorioFactory(@Value("${pelusas.cartas-envenenadas:1}") int cartasEnvenenadas) {
        this.cartasEnvenenadas = cartasEnvenenadas;
    }

    @Override
    public Mazo crear() {
        Mazo mazo = Mazo.estandarConEnvenenadas(cartasEnvenenadas);
        mazo.mezclar(new Random());
        return mazo;
    }
}