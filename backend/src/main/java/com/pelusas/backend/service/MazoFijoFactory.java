package com.pelusas.backend.service;

import com.pelusas.backend.domain.Carta;
import com.pelusas.backend.domain.Mazo;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("e2e")
@Primary
public class MazoFijoFactory implements MazoFactory {

    private static final int[] VALORES = {5, 5, 2, 5, 8, 8, 9, 9, 10, 10};

    @Override
    public Mazo crear() {
        List<Carta> cartas = new ArrayList<>();
        for (int valor : VALORES) {
            cartas.add(new Carta(valor));
        }
        return new Mazo(cartas);
    }
}