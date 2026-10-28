async function llamar(ruta, metodo = 'GET', cuerpo) {
  const respuesta = await fetch(ruta, {
    method: metodo,
    headers: { 'Content-Type': 'application/json' },
    body: cuerpo ? JSON.stringify(cuerpo) : undefined,
  })
  const datos = await respuesta.json().catch(() => null)
  if (!respuesta.ok) {
    throw new Error(datos?.mensaje ?? `Error ${respuesta.status}`)
  }
  return datos
}

export const crearPartida = (jugadores) =>
  llamar('/api/partidas', 'POST', { jugadores })

export const guardarCartas = (id, jugador) =>
  llamar(`/api/partidas/${id}/guardar-cartas`, 'POST', { jugador })

export const sacarCarta = (id, jugador) =>
  llamar(`/api/partidas/${id}/sacar-carta`, 'POST', { jugador })

export const terminarTurno = (id, jugador) =>
  llamar(`/api/partidas/${id}/terminar-turno`, 'POST', { jugador })

export const robarCartas = (id, jugador, victima) =>
  llamar(`/api/partidas/${id}/robar`, 'POST', { jugador, victima })

export const obtenerHistorial = () => llamar('/api/historial')