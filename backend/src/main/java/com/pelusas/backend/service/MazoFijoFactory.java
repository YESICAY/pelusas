package com.pelusas.backend.service;

import com.pelusas.backend.domain.Carta;
import com.pelusas.backend.domain.Mazo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("e2e")
@Primary
public class MazoFijoFactory implements MazoFactory {

    private final int[] valores;

    public MazoFijoFactory(@Value("${pelusas.mazo-fijo:5,5,2,5,8,8,9,9,10,10}") String secuencia) {
        this.valores = Arrays.stream(secuencia.split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    @Override
    public Mazo crear() {
        List<Carta> cartas = new ArrayList<>();
        for (int valor : valores) {
            cartas.add(valor == Carta.VALOR_ENVENENADA ? Carta.pelusaEnvenenada() : new Carta(valor));
        }
        return new Mazo(cartas);
    }
}