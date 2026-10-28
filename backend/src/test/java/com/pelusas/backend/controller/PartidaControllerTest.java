package com.pelusas.backend.controller;

import com.pelusas.backend.repository.PartidaRepository;
import com.pelusas.backend.support.MazoDePruebaConfig;
import com.pelusas.backend.support.MazoDePruebaFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MazoDePruebaConfig.class)
class PartidaControllerTest {

    private static final Pattern ID = Pattern.compile("\"id\":(\\d+)");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MazoDePruebaFactory fabrica;

    @Autowired
    private PartidaRepository repositorio;

    @BeforeEach
    void preparar() {
        fabrica.reiniciar();
        repositorio.deleteAll();
    }

    @AfterEach
    void limpiar() {
        repositorio.deleteAll();
    }

    private ResultActions publicar(String ruta, String cuerpo) throws Exception {
        return mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private long crearPartida(int... valores) throws Exception {
        fabrica.definir(valores);
        String respuesta = publicar("/api/partidas", "{\"jugadores\":[\"Ana\",\"Beto\"]}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Matcher buscador = ID.matcher(respuesta);
        assertTrue(buscador.find());
        return Long.parseLong(buscador.group(1));
    }

    private ResultActions sacar(long id, String jugador) throws Exception {
        return publicar("/api/partidas/" + id + "/sacar-carta", "{\"jugador\":\"" + jugador + "\"}");
    }

    private ResultActions terminar(long id, String jugador) throws Exception {
        return publicar("/api/partidas/" + id + "/terminar-turno", "{\"jugador\":\"" + jugador + "\"}");
    }

    private ResultActions guardar(long id, String jugador) throws Exception {
        return publicar("/api/partidas/" + id + "/guardar-cartas", "{\"jugador\":\"" + jugador + "\"}");
    }

    private ResultActions robar(long id, String jugador, String victima) throws Exception {
        return publicar("/api/partidas/" + id + "/robar",
                "{\"jugador\":\"" + jugador + "\",\"victima\":\"" + victima + "\"}");
    }

    private ResultActions consultar(long id) throws Exception {
        return mvc.perform(get("/api/partidas/" + id));
    }

    @Test
    void crearPartidaDevuelve201ConElEstadoInicial() throws Exception {
        fabrica.definir(1, 2, 3);

        publicar("/api/partidas", "{\"jugadores\":[\"Ana\",\"Beto\"]}")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.terminada").value(false))
                .andExpect(jsonPath("$.turnoDe").value("Ana"))
                .andExpect(jsonPath("$.cartasRestantesEnMazo").value(3))
                .andExpect(jsonPath("$.jugadores.length()").value(2))
                .andExpect(jsonPath("$.ganadores.length()").value(0));
    }

    @Test
    void crearConUnSoloJugadorDevuelve400() throws Exception {
        publicar("/api/partidas", "{\"jugadores\":[\"Ana\"]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void crearSinListaDeJugadoresDevuelve400() throws Exception {
        publicar("/api/partidas", "{}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConUnNombreEnBlancoDevuelve400() throws Exception {
        publicar("/api/partidas", "{\"jugadores\":[\"Ana\",\"\"]}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConNombresRepetidosDevuelve400() throws Exception {
        publicar("/api/partidas", "{\"jugadores\":[\"Ana\",\"Ana\"]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void crearConJsonMalformadoDevuelve400() throws Exception {
        publicar("/api/partidas", "{no es json")
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarUnaPartidaDevuelve200() throws Exception {
        long id = crearPartida(1, 2);

        consultar(id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.turnoDe").value("Ana"));
    }

    @Test
    void consultarUnaPartidaInexistenteDevuelve404() throws Exception {
        consultar(999).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void consultarLosJugadoresDevuelve200() throws Exception {
        long id = crearPartida(1, 2);

        mvc.perform(get("/api/partidas/" + id + "/jugadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Ana"))
                .andExpect(jsonPath("$[1].nombre").value("Beto"));
    }

    @Test
    void sacarCartaCambiaElEstadoDeLaPartida() throws Exception {
        long id = crearPartida(5, 9);

        sacar(id, "Ana")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("CONTINUA"))
                .andExpect(jsonPath("$.partida.jugadores[0].cartasDelante['5']").value(1));

        consultar(id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnoDe").value("Ana"))
                .andExpect(jsonPath("$.cartasRestantesEnMazo").value(1))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante['5']").value(1));
    }

    @Test
    void sacarCartaFueraDeTurnoDevuelve409() throws Exception {
        long id = crearPartida(5, 9);

        sacar(id, "Beto")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void sacarCartaSinNombreDeJugadorDevuelve400() throws Exception {
        long id = crearPartida(5, 9);

        publicar("/api/partidas/" + id + "/sacar-carta", "{}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void sacarCartaEnUnaPartidaInexistenteDevuelve404() throws Exception {
        sacar(999, "Ana").andExpect(status().isNotFound());
    }

    @Test
    void repetirConTresCartasPierdeElTurnoYPasaAlSiguiente() throws Exception {
        long id = crearPartida(5, 2, 3, 5, 9);
        sacar(id, "Ana").andExpect(status().isOk());
        sacar(id, "Ana").andExpect(status().isOk());
        sacar(id, "Ana").andExpect(status().isOk());

        sacar(id, "Ana")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("TURNO_PERDIDO"))
                .andExpect(jsonPath("$.partida.turnoDe").value("Beto"))
                .andExpect(jsonPath("$.partida.jugadores[0].cartasDelante").isEmpty());

        consultar(id)
                .andExpect(jsonPath("$.turnoDe").value("Beto"))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante").isEmpty())
                .andExpect(jsonPath("$.jugadores[0].puntuacion").value(0));
    }

    @Test
    void terminarTurnoPasaAlSiguienteYConservaLasCartas() throws Exception {
        long id = crearPartida(5, 9);
        sacar(id, "Ana").andExpect(status().isOk());

        terminar(id, "Ana")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnoDe").value("Beto"))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante['5']").value(1));
    }

    @Test
    void terminarTurnoSinHaberSacadoDevuelve409() throws Exception {
        long id = crearPartida(5, 9);

        terminar(id, "Ana").andExpect(status().isConflict());
    }

    @Test
    void guardarCartasDespuesDeSacarDevuelve409() throws Exception {
        long id = crearPartida(5, 9);
        sacar(id, "Ana").andExpect(status().isOk());

        guardar(id, "Ana").andExpect(status().isConflict());
    }

    @Test
    void guardarCartasAlInicioDelTurnoLasMueveAlMonton() throws Exception {
        long id = crearPartida(5, 9, 2);
        sacar(id, "Ana").andExpect(status().isOk());
        terminar(id, "Ana").andExpect(status().isOk());
        sacar(id, "Beto").andExpect(status().isOk());
        terminar(id, "Beto").andExpect(status().isOk());

        guardar(id, "Ana")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jugadores[0].puntuacion").value(5))
                .andExpect(jsonPath("$.jugadores[0].cartasEnMonton").value(1))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante").isEmpty());
    }

    @Test
    void robarPasaLasCartasDelMismoValorAlJugadorActual() throws Exception {
        long id = crearPartida(5, 5, 9);
        sacar(id, "Ana").andExpect(status().isOk());
        terminar(id, "Ana").andExpect(status().isOk());
        sacar(id, "Beto").andExpect(status().isOk());

        robar(id, "Beto", "Ana")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jugadores[1].cartasDelante['5']").value(2))
                .andExpect(jsonPath("$.jugadores[0].cartasDelante").isEmpty());
    }

    @Test
    void robarUnValorQueLaVictimaNoTieneDevuelve409() throws Exception {
        long id = crearPartida(5, 3, 9);
        sacar(id, "Ana").andExpect(status().isOk());
        terminar(id, "Ana").andExpect(status().isOk());
        sacar(id, "Beto").andExpect(status().isOk());

        robar(id, "Beto", "Ana").andExpect(status().isConflict());
    }

    @Test
    void robarDeUnJugadorInexistenteDevuelve400() throws Exception {
        long id = crearPartida(5, 9);
        sacar(id, "Ana").andExpect(status().isOk());

        robar(id, "Ana", "Zoe").andExpect(status().isBadRequest());
    }

    @Test
    void robarSinIndicarLaVictimaDevuelve400() throws Exception {
        long id = crearPartida(5, 9);
        sacar(id, "Ana").andExpect(status().isOk());

        publicar("/api/partidas/" + id + "/robar", "{\"jugador\":\"Ana\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void pedirElResultadoDeUnaPartidaEnCursoDevuelve409() throws Exception {
        long id = crearPartida(5, 9);

        mvc.perform(get("/api/partidas/" + id + "/resultado"))
                .andExpect(status().isConflict());
    }

    @Test
    void alTerminarLaPartidaSeConsultaElResultadoYSeGuardaEnElHistorial() throws Exception {
        long id = crearPartida(7);

        sacar(id, "Ana")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("PARTIDA_TERMINADA"))
                .andExpect(jsonPath("$.partida.terminada").value(true));

        mvc.perform(get("/api/partidas/" + id + "/resultado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.terminada").value(true))
                .andExpect(jsonPath("$.ganadores[0]").value("Ana"))
                .andExpect(jsonPath("$.jugadores[0].puntuacion").value(7));

        mvc.perform(get("/api/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jugadores.length()").value(2))
                .andExpect(jsonPath("$[0].jugadores[0].nombre").value("Ana"))
                .andExpect(jsonPath("$[0].jugadores[0].cartas").value("7"))
                .andExpect(jsonPath("$[0].jugadores[0].puntuacion").value(7))
                .andExpect(jsonPath("$[0].jugadores[0].ganador").value(true))
                .andExpect(jsonPath("$[0].jugadores[1].ganador").value(false));

        assertEquals(1L, repositorio.count());
    }

    @Test
    void actuarDespuesDeTerminarLaPartidaDevuelve409() throws Exception {
        long id = crearPartida(7);
        sacar(id, "Ana").andExpect(status().isOk());

        sacar(id, "Ana").andExpect(status().isConflict());
    }

    @Test
    void elHistorialVacioDevuelveUnaListaVacia() throws Exception {
        mvc.perform(get("/api/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}