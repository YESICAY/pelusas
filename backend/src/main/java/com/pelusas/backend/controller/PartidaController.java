package com.pelusas.backend.controller;

import com.pelusas.backend.dto.AccionRequest;
import com.pelusas.backend.dto.CrearPartidaRequest;
import com.pelusas.backend.dto.HistorialDto;
import com.pelusas.backend.dto.JugadorDto;
import com.pelusas.backend.dto.PartidaDto;
import com.pelusas.backend.dto.RobarRequest;
import com.pelusas.backend.dto.SacarCartaDto;
import com.pelusas.backend.service.PartidaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PartidaController {

    private final PartidaService servicio;

    public PartidaController(PartidaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/partidas")
    public ResponseEntity<PartidaDto> crear(@Valid @RequestBody CrearPartidaRequest solicitud) {
        PartidaDto partida = servicio.crear(solicitud.jugadores());
        return ResponseEntity.created(URI.create("/api/partidas/" + partida.id())).body(partida);
    }

    @GetMapping("/partidas/{id}")
    public PartidaDto consultar(@PathVariable long id) {
        return servicio.consultar(id);
    }

    @GetMapping("/partidas/{id}/jugadores")
    public List<JugadorDto> jugadores(@PathVariable long id) {
        return servicio.jugadores(id);
    }

    @PostMapping("/partidas/{id}/guardar-cartas")
    public PartidaDto guardarCartas(@PathVariable long id, @Valid @RequestBody AccionRequest solicitud) {
        return servicio.guardarCartas(id, solicitud.jugador());
    }

    @PostMapping("/partidas/{id}/sacar-carta")
    public SacarCartaDto sacarCarta(@PathVariable long id, @Valid @RequestBody AccionRequest solicitud) {
        return servicio.sacarCarta(id, solicitud.jugador());
    }

    @PostMapping("/partidas/{id}/robar")
    public PartidaDto robar(@PathVariable long id, @Valid @RequestBody RobarRequest solicitud) {
        return servicio.robarCartas(id, solicitud.jugador(), solicitud.victima());
    }

    @PostMapping("/partidas/{id}/terminar-turno")
    public PartidaDto terminarTurno(@PathVariable long id, @Valid @RequestBody AccionRequest solicitud) {
        return servicio.terminarTurno(id, solicitud.jugador());
    }

    @GetMapping("/partidas/{id}/resultado")
    public PartidaDto resultado(@PathVariable long id) {
        return servicio.resultado(id);
    }

    @GetMapping("/historial")
    public List<HistorialDto> historial() {
        return servicio.historial();
    }
}