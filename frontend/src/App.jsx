import { useEffect, useRef, useState } from 'react'
import './App.css'
import * as api from './api'

const MENSAJES = {
  CONTINUA: 'Sacaste una carta. Puedes seguir sacando o terminar tu turno.',
  TURNO_PERDIDO: 'Perdiste el turno: tus cartas fueron descartadas.',
  PARTIDA_TERMINADA: 'Se acabaron las cartas: la partida terminó.',
  PELUSA_ENVENENADA: 'Sacaste una pelusa envenenada. Perdiste el turno: tus cartas fueron descartadas.',
}

const MAX_JUGADORES = 6

function Carta({ valor, nueva = false, resaltada = false, pequena = false }) {
  const clases = ['carta', `v${valor}`]
  if (nueva) clases.push('nueva')
  if (resaltada) clases.push('resaltada')
  if (pequena) clases.push('pequena')
  return (
    <div className={clases.join(' ')} aria-label={`Carta de valor ${valor}`}>
      <span className="esquina arriba" aria-hidden="true">{valor}</span>
      <span className="centro" aria-hidden="true">{valor}</span>
      <span className="esquina abajo" aria-hidden="true">{valor}</span>
    </div>
  )
}

function Reverso({ pequena = false }) {
  return (
    <div className={pequena ? 'carta reverso pequena' : 'carta reverso'} aria-hidden="true">
      <span>P</span>
    </div>
  )
}

function CartaEnvenenada() {
  return (
    <div className="carta envenenada" aria-label="Pelusa envenenada">
      <span className="esquina arriba" aria-hidden="true">☠</span>
      <span className="centro" aria-hidden="true">☠</span>
      <span className="esquina abajo" aria-hidden="true">☠</span>
    </div>
  )
}

function Mazo({ restantes }) {
  const capas = restantes === 0 ? 0 : Math.max(1, Math.min(5, Math.ceil(restantes / 25)))
  return (
    <div className="mazo" aria-hidden="true">
      {capas === 0 && <div className="hueco-mazo">vacío</div>}
      {Array.from({ length: capas }, (_, i) => (
        <div className="capa-mazo" key={i} style={{ '--i': i }}>
          <Reverso />
        </div>
      ))}
      <span className="contador">{restantes}</span>
    </div>
  )
}

function CartasDelante({ cartas }) {
  const grupos = Object.entries(cartas)
  if (grupos.length === 0) {
    return <p className="vacio">Sin cartas delante</p>
  }
  return (
    <div className="grupos">
      {grupos.map(([valor, cantidad]) => (
        <div className="grupo" key={valor}>
          <div className="pila" style={{ '--n': cantidad }}>
            {Array.from({ length: cantidad }, (_, i) => (
              <div className="capa-carta" key={i} style={{ '--i': i }}>
                <Carta valor={Number(valor)} nueva />
              </div>
            ))}
          </div>
          <span className="etiqueta">
            Valor {valor}: {cantidad} carta(s)
          </span>
        </div>
      ))}
    </div>
  )
}

function PanelJugador({ jugador, activo, efecto, ganador }) {
  const clases = ['jugador']
  if (activo) clases.push('turno')
  if (efecto) clases.push(`efecto-${efecto}`)
  if (ganador) clases.push('ganador')
  const cartasMini = Math.min(4, jugador.cartasEnMonton)
  return (
    <div className={clases.join(' ')} data-testid={`jugador-${jugador.nombre}`}>
      <header>
        <span className="avatar">{jugador.nombre.charAt(0).toUpperCase()}</span>
        <h3>
          {jugador.nombre}
          {activo ? ' (su turno)' : ''}
        </h3>
      </header>
      <div className="delante" data-testid={`delante-${jugador.nombre}`}>
        <strong>Cartas delante</strong>
        <CartasDelante cartas={jugador.cartasDelante} />
      </div>
      <div className="monton">
        <div className="mini-pila">
          {Array.from({ length: cartasMini }, (_, i) => (
            <Reverso pequena key={i} />
          ))}
        </div>
        <p data-testid={`monton-${jugador.nombre}`}>
          Montón: {jugador.cartasEnMonton} carta(s) · Puntos: {jugador.puntuacion}
        </p>
      </div>
    </div>
  )
}

function Configuracion({ onCrear }) {
  const [nombres, setNombres] = useState(['', ''])

  const cambiar = (indice, valor) =>
    setNombres(nombres.map((n, i) => (i === indice ? valor : n)))

  return (
    <section className="config">
      <div className="abanico" aria-hidden="true">
        {[1, 3, 5, 7, 10].map((valor, k) => (
          <div className="ficha" key={valor} style={{ '--k': k }}>
            <Carta valor={valor} />
          </div>
        ))}
      </div>
      <h2>Nueva partida</h2>
      {nombres.map((nombre, i) => (
        <div className="fila-nombre" key={i}>
          <span className="avatar">{(nombre.trim().charAt(0) || String(i + 1)).toUpperCase()}</span>
          <input
            data-testid={`nombre-${i}`}
            placeholder={`Jugador ${i + 1}`}
            value={nombre}
            onChange={(e) => cambiar(i, e.target.value)}
          />
          {i >= 2 && (
            <button
              className="boton secundario"
              onClick={() => setNombres(nombres.filter((_, j) => j !== i))}
              aria-label={`Quitar jugador ${i + 1}`}
            >
              ✕
            </button>
          )}
        </div>
      ))}
      <div className="acciones">
        <button
          className="boton secundario"
          disabled={nombres.length >= MAX_JUGADORES}
          onClick={() => setNombres([...nombres, ''])}
        >
          Agregar jugador
        </button>
        <button
          className="boton sacar"
          data-testid="btn-iniciar"
          onClick={() => onCrear(nombres.map((n) => n.trim()))}
        >
          Iniciar partida
        </button>
      </div>
    </section>
  )
}

function Tablero({ partida, ultima, efecto, ocupado, onGuardar, onSacar, onTerminar, onRobar }) {
  const [victima, setVictima] = useState('')
  const otros = partida.jugadores.filter((j) => j.nombre !== partida.turnoDe)
  const victimaElegida = otros.some((j) => j.nombre === victima)
    ? victima
    : (otros[0]?.nombre ?? '')
  const deshabilitado = partida.terminada || ocupado
  const envenenada = ultima?.resultado === 'PELUSA_ENVENENADA'
  const perdida = ultima?.resultado === 'TURNO_PERDIDO' || envenenada

  return (
    <section className="tablero">
      <div className="cabecera-mesa">
        {partida.terminada ? (
          <h2 data-testid="resultado">
            Partida terminada. Ganador(es): {partida.ganadores.join(', ')}
          </h2>
        ) : (
          <h2 data-testid="turno-de">Turno de: {partida.turnoDe}</h2>
        )}
      </div>

      <div className="centro-mesa">
        <div className="zona-mazo">
          <span className="subtitulo">Montón de polvo</span>
          <Mazo restantes={partida.cartasRestantesEnMazo} />
          <p data-testid="cartas-restantes">
            Cartas en el montón de polvo: {partida.cartasRestantesEnMazo}
          </p>
        </div>
        <div className="zona-ultima">
          <span className="subtitulo">Última carta</span>
          {ultima ? (
            <div key={ultima.id} className={perdida ? 'revelada mala' : 'revelada'}>
              {envenenada ? <CartaEnvenenada /> : <Carta valor={ultima.valor} />}
            </div>
          ) : (
            <div className="hueco">—</div>
          )}
          <span className={perdida ? 'etiqueta-ultima mala' : 'etiqueta-ultima'}>
            {envenenada ? '¡Pelusa envenenada! Pierdes el turno' : perdida ? '¡Repetida! Pierdes el turno' : ''}
          </span>
        </div>
      </div>

      {partida.terminada && (
        <div className="fin">
          <h3>Resultado final</h3>
          <ol>
            {[...partida.jugadores]
              .sort((a, b) => b.puntuacion - a.puntuacion)
              .map((j) => {
                const gana = partida.ganadores.includes(j.nombre)
                return (
                  <li key={j.nombre} className={gana ? 'ganador-fila' : ''}>
                    {j.nombre}: {j.puntuacion} puntos ({j.cartasEnMonton} cartas){gana ? ' 🏆' : ''}
                  </li>
                )
              })}
          </ol>
        </div>
      )}

      <div className="jugadores">
        {partida.jugadores.map((j) => (
          <PanelJugador
            key={j.nombre}
            jugador={j}
            activo={j.nombre === partida.turnoDe}
            efecto={efecto?.nombre === j.nombre ? efecto.tipo : null}
            ganador={partida.terminada && partida.ganadores.includes(j.nombre)}
          />
        ))}
      </div>

      <div className="barra-acciones">
        <button className="boton guardar" data-testid="btn-guardar" onClick={onGuardar} disabled={deshabilitado}>
          Guardar cartas
        </button>
        <button className="boton sacar" data-testid="btn-sacar" onClick={onSacar} disabled={deshabilitado}>
          Sacar carta
        </button>
        <button className="boton terminar" data-testid="btn-terminar" onClick={onTerminar} disabled={deshabilitado}>
          Terminar turno
        </button>
        <span className="separador" />
        <label htmlFor="victima">Robar a:</label>
        <select
          id="victima"
          data-testid="select-victima"
          value={victimaElegida}
          onChange={(e) => setVictima(e.target.value)}
          disabled={deshabilitado}
        >
          {otros.map((j) => (
            <option key={j.nombre} value={j.nombre}>
              {j.nombre}
            </option>
          ))}
        </select>
        <button
          className="boton robar"
          data-testid="btn-robar"
          onClick={() => onRobar(victimaElegida)}
          disabled={deshabilitado}
        >
          Robar cartas
        </button>
      </div>
    </section>
  )
}

function Historial({ partidas }) {
  if (partidas.length === 0) {
    return <p data-testid="historial-vacio">Todavía no hay partidas terminadas.</p>
  }
  return (
    <ul className="historial" data-testid="historial">
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
  const [ultima, setUltima] = useState(null)
  const [efecto, setEfecto] = useState(null)
  const [registro, setRegistro] = useState([])
  const [ocupado, setOcupado] = useState(false)
  const contadorCartas = useRef(0)
  const temporizador = useRef(null)

  useEffect(() => () => clearTimeout(temporizador.current), [])

  function lanzarEfecto(nombre, tipo) {
    clearTimeout(temporizador.current)
    setEfecto({ nombre, tipo })
    temporizador.current = setTimeout(() => setEfecto(null), 1000)
  }

  function registrar(texto, entrada = texto) {
    setMensaje(texto)
    setRegistro((anterior) => [entrada, ...anterior].slice(0, 6))
  }

  async function ejecutar(accion, alExito) {
    setError('')
    setOcupado(true)
    try {
      alExito(await accion())
    } catch (e) {
      setError(e.message)
    } finally {
      setOcupado(false)
    }
  }

  const crear = (nombres) =>
    ejecutar(
      () => api.crearPartida(nombres),
      (nueva) => {
        setPartida(nueva)
        setUltima(null)
        setEfecto(null)
        setRegistro([])
        registrar(`Partida creada. Empieza ${nueva.turnoDe}.`)
      },
    )

  const guardar = () => {
    const quien = partida.turnoDe
    return ejecutar(
      () => api.guardarCartas(partida.id, quien),
      (nueva) => {
        setPartida(nueva)
        setUltima(null)
        lanzarEfecto(quien, 'guardado')
        registrar('Cartas guardadas en tu montón de puntos.', `${quien} guardó sus cartas.`)
      },
    )
  }

  const sacar = () => {
    const quien = partida.turnoDe
    return ejecutar(
      () => api.sacarCarta(partida.id, quien),
      (respuesta) => {
        contadorCartas.current += 1
        setUltima({
          id: contadorCartas.current,
          valor: respuesta.carta,
          resultado: respuesta.resultado,
        })
        setPartida(respuesta.partida)
        const siguiente = respuesta.partida.turnoDe
        if (respuesta.resultado === 'TURNO_PERDIDO') {
          lanzarEfecto(quien, 'perdido')
          registrar(
            `${MENSAJES.TURNO_PERDIDO} Ahora juega ${siguiente}.`,
            `${quien} sacó un ${respuesta.carta} repetido y perdió el turno.`,
          )
          } else if (respuesta.resultado === 'PELUSA_ENVENENADA') {
          lanzarEfecto(quien, 'perdido')
          registrar(
            `${MENSAJES.PELUSA_ENVENENADA} Ahora juega ${siguiente}.`,
            `${quien} sacó una pelusa envenenada y perdió el turno.`,
          )
        } else {
          registrar(MENSAJES[respuesta.resultado], `${quien} sacó un ${respuesta.carta}.`)
        }
      },
    )
  }

  const terminar = () => {
    const quien = partida.turnoDe
    return ejecutar(
      () => api.terminarTurno(partida.id, quien),
      (nueva) => {
        setPartida(nueva)
        setUltima(null)
        registrar(
          `Turno terminado. Ahora juega ${nueva.turnoDe}.`,
          `${quien} terminó su turno.`,
        )
      },
    )
  }

  const robar = (victima) => {
    const quien = partida.turnoDe
    return ejecutar(
      () => api.robarCartas(partida.id, quien, victima),
      (nueva) => {
        setPartida(nueva)
        lanzarEfecto(quien, 'robo')
        registrar(`Le robaste cartas a ${victima}.`, `${quien} le robó cartas a ${victima}.`)
      },
    )
  }

  const cargarHistorial = () =>
    ejecutar(
      () => api.obtenerHistorial(),
      (lista) => setHistorial(lista),
    )

  return (
    <main className="juego">
      <div className="contenedor">
        <header className="titulo">
          <h1>Pelusas</h1>
          <p>Junta pelusas, no repitas y roba las de tus rivales</p>
        </header>

        {error && (
          <p className="error" data-testid="error" role="alert">
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
              ultima={ultima}
              efecto={efecto}
              ocupado={ocupado}
              onGuardar={guardar}
              onSacar={sacar}
              onTerminar={terminar}
              onRobar={robar}
            />
            {registro.length > 0 && (
              <ol className="registro">
                {registro.map((entrada, i) => (
                  <li key={`${i}-${entrada}`}>{entrada}</li>
                ))}
              </ol>
            )}
            <div className="pie">
              <button
                className="boton secundario"
                onClick={() => {
                  setPartida(null)
                  setMensaje('')
                  setUltima(null)
                  setRegistro([])
                }}
              >
                Nueva partida
              </button>
            </div>
          </>
        ) : (
          <Configuracion onCrear={crear} />
        )}

        <div className="pie">
          <button className="boton secundario" data-testid="btn-historial" onClick={cargarHistorial}>
            Ver historial
          </button>
        </div>
        {historial && <Historial partidas={historial} />}
      </div>
    </main>
  )
}