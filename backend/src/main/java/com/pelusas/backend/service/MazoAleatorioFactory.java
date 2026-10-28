package com.pelusas.backend.service;

import com.pelusas.backend.domain.Mazo;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MazoAleatorioFactory implements MazoFactory {

    @Override
    public Mazo crear() {
        Mazo mazo = Mazo.estandar();
        mazo.mezclar(new Random());
        return mazo;
    }
}