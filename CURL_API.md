# CURLs de la API de Pelusas
 
Este documento reúne los comandos `curl` para probar **todos los endpoints** del backend sin usar el frontend.
Sirven para comprobar que la API funciona por sí sola y que las reglas del juego las valida el backend.
 
Todos los ejemplos asumen el backend en `http://localhost:8080` y están escritos para **bash**.
 
## Preparación
 
Para reproducir **exactamente** los ejemplos de este documento, arranca el backend con el perfil `e2e`,
que usa un mazo fijo de 10 cartas (`5, 5, 2, 5, 8, 8, 9, 9, 10, 10`, la primera es la de arriba)
en lugar de uno aleatorio, y una base de datos en memoria. Las comillas son necesarias en PowerShell y
también funcionan en bash:
 
```bash
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=e2e"
```
 
Con el arranque normal (`mvn spring-boot:run`) todo funciona igual, pero el mazo es el real, de 110 cartas
mezcladas al azar, y los resultados varían.
 
Notas:
 
- Las partidas en curso viven en memoria: si reinicias el servidor, los ids vuelven a empezar en 1.
- Ejecuta los escenarios **en orden** y sin reiniciar el servidor entre ellos: el escenario A usa la partida 1,
  el B la partida 2 y el C continúa con la partida 2.
- Si usas **PowerShell** en lugar de Git Bash, escribe `curl.exe` (en PowerShell `curl` es otro comando) y
  pasa el JSON desde un archivo para evitar problemas con las comillas:
  guarda el cuerpo en `cuerpo.json` y usa `-d "@cuerpo.json"`.
## Resumen de endpoints
 
| Método y ruta | Qué hace | Éxito |
|---|---|---|
| `POST /api/partidas` | Crear una partida | 201 |
| `GET /api/partidas/{id}` | Consultar el estado de la partida | 200 |
| `GET /api/partidas/{id}/jugadores` | Consultar los jugadores | 200 |
| `POST /api/partidas/{id}/guardar-cartas` | Guardar las cartas en el montón de puntos | 200 |
| `POST /api/partidas/{id}/sacar-carta` | Sacar una carta del montón de polvo | 200 |
| `POST /api/partidas/{id}/robar` | Robar cartas del mismo valor a otro jugador | 200 |
| `POST /api/partidas/{id}/terminar-turno` | Terminar el turno voluntariamente | 200 |
| `GET /api/partidas/{id}/resultado` | Consultar el resultado (partida terminada) | 200 |
| `GET /api/historial` | Consultar el histórico de partidas | 200 |
 
"Continuar jugando" es volver a llamar a `sacar-carta`, y "parar" es llamar a `terminar-turno`.
 
## 1. Crear una partida (201)
 
```bash
curl -i -X POST http://localhost:8080/api/partidas \
  -H "Content-Type: application/json" \
  -d '{"jugadores":["Ana","Beto"]}'
```
 
Respuesta esperada (con el mazo fijo):
 
```json
{"id":1,"terminada":false,"turnoDe":"Ana","cartasRestantesEnMazo":10,
 "jugadores":[{"nombre":"Ana","cartasDelante":{},"cartasEnMonton":0,"puntuacion":0},
              {"nombre":"Beto","cartasDelante":{},"cartasEnMonton":0,"puntuacion":0}],
 "ganadores":[]}
```
 
## 2. Consultar el estado de una partida (200)
 
```bash
curl -i http://localhost:8080/api/partidas/1
```
 
## 3. Consultar los jugadores (200)
 
```bash
curl -i http://localhost:8080/api/partidas/1/jugadores
```
 
## 4. Sacar una carta (200)
 
La respuesta incluye el `resultado` (`CONTINUA`, `TURNO_PERDIDO` o `PARTIDA_TERMINADA`), el valor de la
`carta` sacada (aunque haya hecho perder el turno) y el estado actualizado de la `partida`.
Volver a llamarlo equivale a "continuar jugando".
 
```bash
curl -i -X POST http://localhost:8080/api/partidas/1/sacar-carta \
  -H "Content-Type: application/json" \
  -d '{"jugador":"Ana"}'
```
 
Respuesta esperada para la primera carta del mazo fijo (resumida):
 
```json
{"resultado":"CONTINUA","carta":5,"partida":{"id":1,"terminada":false,"turnoDe":"Ana", ...}}
```
 
## 5. Terminar el turno voluntariamente (200)
 
Exige haber sacado al menos una carta en el turno.
 
```bash
curl -i -X POST http://localhost:8080/api/partidas/1/terminar-turno \
  -H "Content-Type: application/json" \
  -d '{"jugador":"Ana"}'
```
 
## 6. Guardar las cartas en el montón de puntos (200)
 
Solo se puede al inicio del turno, antes de sacar una carta.
 
```bash
curl -i -X POST http://localhost:8080/api/partidas/1/guardar-cartas \
  -H "Content-Type: application/json" \
  -d '{"jugador":"Ana"}'
```
 
## 7. Robar cartas (200)
 
Solo después de sacar una carta en el turno. Se roban las cartas del **mismo valor** que la carta
recién sacada que estén boca arriba delante de la víctima indicada.
 
```bash
curl -i -X POST http://localhost:8080/api/partidas/2/robar \
  -H "Content-Type: application/json" \
  -d '{"jugador":"Beto","victima":"Ana"}'
```
 
## 8. Consultar el resultado (200, solo si la partida terminó)
 
```bash
curl -i http://localhost:8080/api/partidas/1/resultado
```
 
## 9. Consultar el histórico (200)
 
Lista las partidas terminadas, de la más reciente a la más antigua.
 
```bash
curl -i http://localhost:8080/api/historial
```
 
---
 
## Escenario A: turno perdido y partida completa (mazo fijo, partida 1)
 
Mazo: `5, 5, 2, 5, 8, 8, 9, 9, 10, 10`.
 
```bash
# Crear la partida
curl -s -X POST http://localhost:8080/api/partidas -H "Content-Type: application/json" -d '{"jugadores":["Ana","Beto"]}'
 
# Ana saca 5, 5 y 2: CONTINUA (ya tiene 3 cartas delante)
curl -s -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Ana"}'
curl -s -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Ana"}'
curl -s -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Ana"}'
 
# Ana saca otro 5: repite con 3 cartas delante -> TURNO_PERDIDO, turno de Beto, cartas de Ana descartadas
curl -s -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Ana"}'
 
# Beto saca 8, 8 y 9 (CONTINUA) y luego otro 9 -> TURNO_PERDIDO, turno de Ana
curl -s -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Beto"}'
curl -s -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Beto"}'
curl -s -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Beto"}'
curl -s -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Beto"}'
 
# Ana saca 10 y 10; con la última carta se agota el montón -> PARTIDA_TERMINADA
curl -s -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Ana"}'
curl -s -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Ana"}'
 
# Resultado: gana Ana con 20 puntos; Beto tiene 0
curl -i http://localhost:8080/api/partidas/1/resultado
 
# La partida quedó guardada en el histórico
curl -i http://localhost:8080/api/historial
```
 
## Escenario B: robo de cartas (mazo fijo, partida 2)
 
```bash
curl -s -X POST http://localhost:8080/api/partidas -H "Content-Type: application/json" -d '{"jugadores":["Ana","Beto"]}'
 
# Ana saca un 5 y termina el turno
curl -s -X POST http://localhost:8080/api/partidas/2/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Ana"}'
curl -s -X POST http://localhost:8080/api/partidas/2/terminar-turno -H "Content-Type: application/json" -d '{"jugador":"Ana"}'
 
# Beto saca otro 5 y le roba a Ana el 5 que tenía delante: Beto queda con dos 5 y Ana con ninguna
curl -s -X POST http://localhost:8080/api/partidas/2/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Beto"}'
curl -i -X POST http://localhost:8080/api/partidas/2/robar -H "Content-Type: application/json" -d '{"jugador":"Beto","victima":"Ana"}'
```
 
## Escenario C: errores (continúa con la partida 2, turno de Beto)
 
```bash
# 400: menos de 2 jugadores
curl -i -X POST http://localhost:8080/api/partidas -H "Content-Type: application/json" -d '{"jugadores":["Ana"]}'
 
# 400: nombres repetidos
curl -i -X POST http://localhost:8080/api/partidas -H "Content-Type: application/json" -d '{"jugadores":["Ana","Ana"]}'
 
# 400: JSON mal formado
curl -i -X POST http://localhost:8080/api/partidas -H "Content-Type: application/json" -d '{no es json'
 
# 400: falta indicar la víctima
curl -i -X POST http://localhost:8080/api/partidas/2/robar -H "Content-Type: application/json" -d '{"jugador":"Beto"}'
 
# 400: la víctima no existe
curl -i -X POST http://localhost:8080/api/partidas/2/robar -H "Content-Type: application/json" -d '{"jugador":"Beto","victima":"Zoe"}'
 
# 404: la partida no existe
curl -i http://localhost:8080/api/partidas/999
 
# 409: sacar carta fuera de turno (le toca a Beto)
curl -i -X POST http://localhost:8080/api/partidas/2/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Ana"}'
 
# 409: guardar cartas después de haber sacado una carta en el turno
curl -i -X POST http://localhost:8080/api/partidas/2/guardar-cartas -H "Content-Type: application/json" -d '{"jugador":"Beto"}'
 
# 409: pedir el resultado de una partida que sigue en curso
curl -i http://localhost:8080/api/partidas/2/resultado
 
# 409: actuar sobre una partida terminada (la partida 1 terminó en el escenario A)
curl -i -X POST http://localhost:8080/api/partidas/1/sacar-carta -H "Content-Type: application/json" -d '{"jugador":"Ana"}'
```
 
Las respuestas de error tienen siempre esta forma:
 
```json
{"status":409,"mensaje":"No es el turno de Ana"}
```
 
## Códigos de error
 
| Código | Significado |
|---|---|
| 400 | Datos inválidos: menos de 2 jugadores, nombres repetidos o vacíos, JSON roto, jugador inexistente. |
| 404 | La partida no existe. |
| 409 | La petición es válida pero no se puede hacer en el estado actual: fuera de turno, partida terminada, guardar tras sacar, terminar sin sacar, robar sin cartas del valor sacado. |
