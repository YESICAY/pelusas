package com.pelusas.backend.service;

import com.pelusas.backend.domain.Partida;
import com.pelusas.backend.dto.HistorialDto;
import com.pelusas.backend.dto.JugadorDto;
import com.pelusas.backend.dto.PartidaDto;
import com.pelusas.backend.dto.PartidaMapper;
import com.pelusas.backend.dto.SacarCartaDto;
import com.pelusas.backend.exception.PartidaNoEncontradaException;
import com.pelusas.backend.repository.PartidaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PartidaService {

    private final PartidaRepository repositorio;
    private final MazoFactory mazoFactory;
    private final Map<Long, Partida> partidas = new HashMap<>();
    private long siguienteId = 1;

    public PartidaService(PartidaRepository repositorio, MazoFactory mazoFactory) {
        this.repositorio = repositorio;
        this.mazoFactory = mazoFactory;
    }

    public synchronized PartidaDto crear(List<String> nombres) {
        Partida partida = new Partida(nombres, mazoFactory.crear());
        long id = siguienteId++;
        partidas.put(id, partida);
        return PartidaMapper.partida(id, partida);
    }

    public synchronized PartidaDto consultar(long id) {
        return PartidaMapper.partida(id, buscar(id));
    }

    public synchronized List<JugadorDto> jugadores(long id) {
        return buscar(id).jugadores().stream()
                .map(PartidaMapper::jugador)
                .toList();
    }

    public synchronized PartidaDto resultado(long id) {
        Partida partida = buscar(id);
        if (!partida.estaTerminada()) {
            throw new IllegalStateException("La partida aun no termina");
        }
        return PartidaMapper.partida(id, partida);
    }

    public synchronized PartidaDto guardarCartas(long id, String jugador) {
        Partida partida = partidaEnTurnoDe(id, jugador);
        partida.guardarCartas();
        return PartidaMapper.partida(id, partida);
    }

    public synchronized SacarCartaDto sacarCarta(long id, String jugador) {
        Partida partida = partidaEnTurnoDe(id, jugador);
        Partida.ResultadoSacar resultado = partida.sacarCarta();
        if (partida.estaTerminada()) {
            repositorio.save(PartidaMapper.entidad(partida, LocalDateTime.now()));
        }
        return new SacarCartaDto(resultado.name(), PartidaMapper.partida(id, partida));
    }

    public synchronized PartidaDto robarCartas(long id, String jugador, String victima) {
        Partida partida = partidaEnTurnoDe(id, jugador);
        partida.robarCartas(victima);
        return PartidaMapper.partida(id, partida);
    }

    public synchronized PartidaDto terminarTurno(long id, String jugador) {
        Partida partida = partidaEnTurnoDe(id, jugador);
        partida.finalizarTurno();
        return PartidaMapper.partida(id, partida);
    }

    public List<HistorialDto> historial() {
        return repositorio.findAllByOrderByFechaDesc().stream()
                .map(PartidaMapper::historial)
                .toList();
    }

    private Partida buscar(long id) {
        Partida partida = partidas.get(id);
        if (partida == null) {
            throw new PartidaNoEncontradaException(id);
        }
        return partida;
    }

    private Partida partidaEnTurnoDe(long id, String jugador) {
        Partida partida = buscar(id);
        if (!partida.estaTerminada() && !partida.jugadorActual().nombre().equals(jugador)) {
            throw new IllegalStateException("No es el turno de " + jugador);
        }
        return partida;
    }
}