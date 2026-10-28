import { useState } from 'react'
import './App.css'
import * as api from './api'

const MENSAJES = {
  CONTINUA: 'Sacaste una carta. Puedes seguir sacando o terminar tu turno.',
  TURNO_PERDIDO: 'Perdiste el turno: tus cartas fueron descartadas.',
  PARTIDA_TERMINADA: 'Se acabaron las cartas: la partida terminó.',
}

function Cartas({ cartas }) {
  const grupos = Object.entries(cartas)
  if (grupos.length === 0) {
    return <p>Sin cartas delante</p>
  }
  return (
    <ul>
      {grupos.map(([valor, cantidad]) => (
        <li key={valor}>
          Valor {valor}: {cantidad} carta(s)
        </li>
      ))}
    </ul>
  )
}

function Configuracion({ onCrear }) {
  const [nombres, setNombres] = useState(['', ''])

  const cambiar = (indice, valor) =>
    setNombres(nombres.map((n, i) => (i === indice ? valor : n)))

  return (
    <section>
      <h2>Nueva partida</h2>
      {nombres.map((nombre, i) => (
        <input
          key={i}
          data-testid={`nombre-${i}`}
          placeholder={`Jugador ${i + 1}`}
          value={nombre}
          onChange={(e) => cambiar(i, e.target.value)}
        />
      ))}
      <div className="acciones">
        <button onClick={() => setNombres([...nombres, ''])}>Agregar jugador</button>
        <button
          data-testid="btn-iniciar"
          onClick={() => onCrear(nombres.map((n) => n.trim()))}
        >
          Iniciar partida
        </button>
      </div>
    </section>
  )
}

function Jugador({ jugador, esTurno }) {
  return (
    <div
      className={esTurno ? 'jugador turno' : 'jugador'}
      data-testid={`jugador-${jugador.nombre}`}
    >
      <h3>
        {jugador.nombre}
        {esTurno ? ' (su turno)' : ''}
      </h3>
      <div data-testid={`delante-${jugador.nombre}`}>
        <strong>Cartas delante</strong>
        <Cartas cartas={jugador.cartasDelante} />
      </div>
      <p data-testid={`monton-${jugador.nombre}`}>
        Montón: {jugador.cartasEnMonton} carta(s) · Puntos: {jugador.puntuacion}
      </p>
    </div>
  )
}

function Tablero({ partida, onGuardar, onSacar, onTerminar, onRobar }) {
  const [victima, setVictima] = useState('')
  const otros = partida.jugadores.filter((j) => j.nombre !== partida.turnoDe)
  const victimaElegida = otros.some((j) => j.nombre === victima)
    ? victima
    : (otros[0]?.nombre ?? '')

  return (
    <section>
      {partida.terminada ? (
        <h2 data-testid="resultado">
          Partida terminada. Ganador(es): {partida.ganadores.join(', ')}
        </h2>
      ) : (
        <h2 data-testid="turno-de">Turno de: {partida.turnoDe}</h2>
      )}
      <p data-testid="cartas-restantes">
        Cartas en el montón de polvo: {partida.cartasRestantesEnMazo}
      </p>

      <div className="acciones">
        <button data-testid="btn-guardar" onClick={onGuardar} disabled={partida.terminada}>
          Guardar cartas
        </button>
        <button data-testid="btn-sacar" onClick={onSacar} disabled={partida.terminada}>
          Sacar carta
        </button>
        <button data-testid="btn-terminar" onClick={onTerminar} disabled={partida.terminada}>
          Terminar turno
        </button>
      </div>

      <div className="acciones">
        <select
          data-testid="select-victima"
          value={victimaElegida}
          onChange={(e) => setVictima(e.target.value)}
          disabled={partida.terminada}
        >
          {otros.map((j) => (
            <option key={j.nombre} value={j.nombre}>
              {j.nombre}
            </option>
          ))}
        </select>
        <button
          data-testid="btn-robar"
          onClick={() => onRobar(victimaElegida)}
          disabled={partida.terminada}
        >
          Robar cartas
        </button>
      </div>

      <div className="jugadores">
        {partida.jugadores.map((j) => (
          <Jugador key={j.nombre} jugador={j} esTurno={j.nombre === partida.turnoDe} />
        ))}
      </div>
    </section>
  )
}

function Historial({ partidas }) {
  if (partidas.length === 0) {
    return <p data-testid="historial-vacio">Todavía no hay partidas terminadas.</p>
  }
  return (
    <ul data-testid="historial">
      {partidas.map((p) => (
        <li key={p.id}>
          <strong>Partida {p.id}</strong> ({p.fecha})
          <ul>
            {p.jugadores.map((j) => (
              <li key={j.nombre}>
                {j.nombre}: {j.puntuacion} puntos [{j.cartas}]{j.ganador ? ' 🏆' : ''}
              </li>
            ))}
          </ul>
        </li>
      ))}
    </ul>
  )
}

export default function App() {
  const [partida, setPartida] = useState(null)
  const [mensaje, setMensaje] = useState('')
  const [error, setError] = useState('')
  const [historial, setHistorial] = useState(null)

  async function ejecutar(accion, alExito) {
    setError('')
    try {
      alExito(await accion())
    } catch (e) {
      setError(e.message)
    }
  }

  const crear = (nombres) =>
    ejecutar(
      () => api.crearPartida(nombres),
      (nueva) => {
        setPartida(nueva)
        setMensaje(`Partida creada. Empieza ${nueva.turnoDe}.`)
      },
    )

  const guardar = () =>
    ejecutar(
      () => api.guardarCartas(partida.id, partida.turnoDe),
      (nueva) => {
        setPartida(nueva)
        setMensaje('Cartas guardadas en tu montón de puntos.')
      },
    )

  const sacar = () =>
    ejecutar(
      () => api.sacarCarta(partida.id, partida.turnoDe),
      (respuesta) => {
        setPartida(respuesta.partida)
        const siguiente = respuesta.partida.turnoDe
        const extra = respuesta.resultado === 'TURNO_PERDIDO' ? ` Ahora juega ${siguiente}.` : ''
        setMensaje(MENSAJES[respuesta.resultado] + extra)
      },
    )

  const terminar = () =>
    ejecutar(
      () => api.terminarTurno(partida.id, partida.turnoDe),
      (nueva) => {
        setPartida(nueva)
        setMensaje(`Turno terminado. Ahora juega ${nueva.turnoDe}.`)
      },
    )

  const robar = (victima) =>
    ejecutar(
      () => api.robarCartas(partida.id, partida.turnoDe, victima),
      (nueva) => {
        setPartida(nueva)
        setMensaje(`Le robaste cartas a ${victima}.`)
      },
    )

  const cargarHistorial = () =>
    ejecutar(
      () => api.obtenerHistorial(),
      (lista) => setHistorial(lista),
    )

  return (
    <main>
      <h1>Pelusas</h1>
      {error && (
        <p className="error" data-testid="error">
          {error}
        </p>
      )}
      {mensaje && (
        <p className="mensaje" data-testid="mensaje">
          {mensaje}
        </p>
      )}

      {partida ? (
        <>
          <Tablero
            partida={partida}
            onGuardar={guardar}
            onSacar={sacar}
            onTerminar={terminar}
            onRobar={robar}
          />
          <button
            onClick={() => {
              setPartida(null)
              setMensaje('')
            }}
          >
            Nueva partida
          </button>
        </>
      ) : (
        <Configuracion onCrear={crear} />
      )}

      <hr />
      <button data-testid="btn-historial" onClick={cargarHistorial}>
        Ver historial
      </button>
      {historial && <Historial partidas={historial} />}
    </main>
  )
}