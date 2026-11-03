# Colección de Comandos cURL - API REST Pelusas

Este documento contiene todos los comandos cURL estructurados para verificar y probar los servicios REST del backend de Pelusas.

## Requisitos previos
Asegurarse de que el backend esté ejecutándose en `http://localhost:8080`.

---

### 1. Crear una nueva partida
Crea e inicializa una partida con los jugadores indicados y genera el mazo inicial.

```bash
curl -X POST http://localhost:8080/api/partidas \
  -H "Content-Type: application/json" \
  -d '{
    "jugadores": ["Jugador 1", "Jugador 2", "Jugador 3"]
  }'
