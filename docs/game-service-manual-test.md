# Prueba manual del Game Service

Esta guia sirve para levantar el backend y probar el flujo basico de una partida desde terminal.

## 1. Levantar infraestructura

Desde la raiz del proyecto:

```bash
cd /Users/franciscorodriguezpons/Documents/pokemon-new/POKEMON-TCG
docker compose up -d postgres redis
```

Puertos esperados:

- Postgres: `localhost:5433`
- Redis: `localhost:6380`
- Game Service: `localhost:8081`

## 2. Levantar Game Service

En una terminal dedicada:

```bash
cd /Users/franciscorodriguezpons/Documents/pokemon-new/POKEMON-TCG
mvn -pl game-service spring-boot:run
```

Dejar esa terminal abierta. Si se hacen cambios en codigo Java, cortar con `Ctrl + C` y volver a levantar el servicio.

## 3. Preparar IDs de jugadores

En otra terminal:

```bash
cd /Users/franciscorodriguezpons/Documents/pokemon-new/POKEMON-TCG

P1=033B03A8-BD43-40F1-A95F-15D75CBD1D8E
P2=DA301844-E228-42DB-838B-3BDEAC2D8D38
```

## 4. Crear mazos reales y guardar sus IDs

El flujo recomendado ahora es crear un mazo por jugador y usar esos `deckId` al crear/unirse a la partida.

```bash
cat > /tmp/deck-p1.json <<JSON
{
  "playerId": "$P1",
  "name": "Mazo P1 XY1 Test",
  "cards": [
    {
      "cardId": "xy1-1",
      "name": "Basic Test",
      "setId": "xy1",
      "quantity": 4,
      "type": "Pokemon",
      "subtype": "Basic",
      "basicEnergy": false,
      "basicPokemon": true,
      "aceSpec": false,
      "hp": 120,
      "attackDamage": 30,
      "attackRequiredEnergy": 1
    },
    {
      "cardId": "xy1-2",
      "name": "Basic Energy",
      "setId": "xy1",
      "quantity": 56,
      "type": "Energy",
      "subtype": "Basic",
      "basicEnergy": true,
      "basicPokemon": false,
      "aceSpec": false
    }
  ]
}
JSON

cat > /tmp/deck-p2.json <<JSON
{
  "playerId": "$P2",
  "name": "Mazo P2 XY1 Test",
  "cards": [
    {
      "cardId": "xy1-1",
      "name": "Basic Test",
      "setId": "xy1",
      "quantity": 4,
      "type": "Pokemon",
      "subtype": "Basic",
      "basicEnergy": false,
      "basicPokemon": true,
      "aceSpec": false,
      "hp": 120,
      "attackDamage": 30,
      "attackRequiredEnergy": 1
    },
    {
      "cardId": "xy1-2",
      "name": "Basic Energy",
      "setId": "xy1",
      "quantity": 56,
      "type": "Energy",
      "subtype": "Basic",
      "basicEnergy": true,
      "basicPokemon": false,
      "aceSpec": false
    }
  ]
}
JSON

DECK1_RES=$(curl -s -X POST http://localhost:8081/api/decks \
  -H "Content-Type: application/json" \
  -d @/tmp/deck-p1.json)

DECK2_RES=$(curl -s -X POST http://localhost:8081/api/decks \
  -H "Content-Type: application/json" \
  -d @/tmp/deck-p2.json)

DECK1_ID=$(printf '%s' "$DECK1_RES" | python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])')
DECK2_ID=$(printf '%s' "$DECK2_RES" | python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])')

echo "DECK1_ID=$DECK1_ID"
echo "DECK2_ID=$DECK2_ID"
```

## 5. Crear partida con deckId del jugador 1

```bash
CREATE_RES=$(curl -s -X POST http://localhost:8081/api/games \
  -H "Content-Type: application/json" \
  -d "{\"playerId\":\"$P1\",\"deckId\":\"$DECK1_ID\"}")

echo "$CREATE_RES"

GAME_ID=$(printf '%s' "$CREATE_RES" | python3 -c 'import sys,json; print(json.load(sys.stdin)["gameId"])')
echo "GAME_ID=$GAME_ID"
```

Resultado esperado:

- `state`: `WAITING`
- `phase`: `DRAW`
- se imprime un `GAME_ID`

## 6. Unir jugador 2 con deckId

```bash
curl -s -X POST "http://localhost:8081/api/games/$GAME_ID/join" \
  -H "Content-Type: application/json" \
  -d "{\"playerId\":\"$P2\",\"deckId\":\"$DECK2_ID\"}" | python3 -m json.tool
```

Resultado esperado:

- la partida sigue en `WAITING`
- todavia no hay setup de mesa

## 7. Ejecutar setup inicial desde mazos reales

```bash
curl -s -X POST "http://localhost:8081/api/games/$GAME_ID/setup" \
  -H "Content-Type: application/json" \
  -d '{}' | python3 -m json.tool
```

Resultado esperado:

- `state`: `ACTIVE`
- `phase`: `MAIN`
- hay Pokemon activo para ambos jugadores
- hay 6 premios para cada jugador
- `deckCardsRemainingByPlayer` deberia quedar cerca de `47` para cada jugador

Si queres probar el flujo viejo sin mazos guardados, el endpoint todavia acepta `playerOneDeckSize`, `playerOneBasicCount`, `playerTwoDeckSize` y `playerTwoBasicCount`.

## 8. Avanzar hasta el turno de P2

El primer `END_TURN` pasa de `MAIN` a `ATTACK`.

```bash
curl -s -X POST "http://localhost:8081/api/games/$GAME_ID/actions" \
  -H "Content-Type: application/json" \
  -d '{"actionType":"END_TURN"}' | python3 -m json.tool
```

El segundo `END_TURN` pasa de `ATTACK` a `BETWEEN_TURNS`.

```bash
curl -s -X POST "http://localhost:8081/api/games/$GAME_ID/actions" \
  -H "Content-Type: application/json" \
  -d '{"actionType":"END_TURN"}' | python3 -m json.tool
```

El tercer `END_TURN` cambia el turno a `P2` y vuelve a `DRAW`.

```bash
curl -s -X POST "http://localhost:8081/api/games/$GAME_ID/actions" \
  -H "Content-Type: application/json" \
  -d '{"actionType":"END_TURN"}' | python3 -m json.tool
```

Resultado esperado:

- `phase`: `DRAW`
- `currentTurnPlayer`: valor de `P2`

## 9. Robar carta con P2

```bash
curl -s -X POST "http://localhost:8081/api/games/$GAME_ID/actions" \
  -H "Content-Type: application/json" \
  -d '{"actionType":"DRAW"}' | python3 -m json.tool
```

Resultado esperado:

- `phase`: `MAIN`
- la mano de `P2` sube en 1
- el mazo de `P2` baja en 1

Ejemplo esperado si antes tenia 47 cartas:

- antes: `deckCardsRemainingByPlayer[P2] = 47`
- despues: `deckCardsRemainingByPlayer[P2] = 46`

Si el mazo sube en lugar de bajar, hay un problema de sincronizacion entre el contador publico y la lista real del mazo.

## 10. Ver estado actual

```bash
curl -s "http://localhost:8081/api/games/$GAME_ID" | python3 -m json.tool
```

## 11. Ver sync/reconexion

```bash
curl -s "http://localhost:8081/api/games/$GAME_ID/sync?since=0" | python3 -m json.tool
```

Este endpoint devuelve el estado actual de la partida y los eventos pendientes desde la secuencia indicada.

## 12. Ver action log

```bash
curl -s "http://localhost:8081/api/games/$GAME_ID/actions" | python3 -m json.tool
```

Resultado esperado:

- devuelve `entries`
- cada entrada muestra fecha, jugador, accion y resultado

## 13. Acciones utiles para seguir probando

Adjuntar energia:

```bash
curl -s -X POST "http://localhost:8081/api/games/$GAME_ID/actions" \
  -H "Content-Type: application/json" \
  -d '{"actionType":"ATTACH_ENERGY"}' | python3 -m json.tool
```

Si se intenta adjuntar una segunda energia en el mismo turno, la API debe responder `400 Bad Request` con un mensaje claro.

Atacar, solo si la fase es `ATTACK`:

```bash
curl -s -X POST "http://localhost:8081/api/games/$GAME_ID/actions" \
  -H "Content-Type: application/json" \
  -d '{"actionType":"ATTACK"}' | python3 -m json.tool
```

Aplicar condicion especial:

```bash
curl -s -X POST "http://localhost:8081/api/games/$GAME_ID/actions" \
  -H "Content-Type: application/json" \
  -d '{"actionType":"APPLY_SPECIAL_CONDITION","condition":"POISONED"}' | python3 -m json.tool
```

Condiciones soportadas:

- `ASLEEP`
- `PARALYZED`
- `CONFUSED`
- `POISONED`
- `BURNED`

Resolver entre turnos:

```bash
curl -s -X POST "http://localhost:8081/api/games/$GAME_ID/actions" \
  -H "Content-Type: application/json" \
  -d '{"actionType":"RESOLVE_BETWEEN_TURNS"}' | python3 -m json.tool
```

Resultado esperado:

- resuelve dano/efectos de condiciones especiales
- pasa automaticamente al proximo jugador
- deja la fase en `DRAW`, salvo que la partida termine

## 14. Limpiar datos si hace falta

Para borrar datos persistidos y empezar desde cero:

```bash
docker compose down -v
docker compose up -d postgres redis
```

Despues volver a levantar `game-service`.
