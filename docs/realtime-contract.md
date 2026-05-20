# Contrato realtime

## Decision de almacenamiento

En esta etapa `realtime-service` mantiene un buffer en memoria de eventos recientes por partida.

- Guarda hasta 500 eventos por `gameId`.
- Cada evento emitido recibe un `sequence` monotono.
- La reconexion se hace pidiendo eventos posteriores a `sinceSequence`.
- Si `realtime-service` se reinicia, el buffer se pierde.
- La fuente de verdad sigue siendo `game-service`, mediante estado actual, snapshots y action log.

Para una reconexion robusta del frontend:

1. pedir estado actual a `game-service`;
2. pedir eventos pendientes a `realtime-service` usando el ultimo `sequence` visto;
3. si faltan eventos por reinicio del buffer, usar el estado actual de `game-service` como verdad.

## Envelope de evento

Todo evento que `realtime-service` emite por WebSocket usa este formato:

```json
{
  "sequence": 1,
  "schemaVersion": 1,
  "gameId": "uuid",
  "type": "ENERGY_ATTACHED",
  "payload": {
    "attachedEnergy": 1
  },
  "occurredAt": "2026-05-06T18:00:00Z",
  "receivedAt": "2026-05-06T18:00:01Z"
}
```

## Tipos soportados

- `GAME_CREATED`
- `PLAYER_JOINED`
- `GAME_SETUP`
- `SETUP_COMPLETED`
- `TURN_STARTED`
- `TURN_PHASE_CHANGED`
- `CARD_DRAWN`
- `ENERGY_ATTACHED`
- `SUPPORTER_PLAYED`
- `RETREAT_DECLARED`
- `ATTACK_RESOLVED`
- `KO`
- `PRIZE_TAKEN`
- `STATUS_APPLIED`
- `BETWEEN_TURNS_RESOLVED`
- `GAME_FINISHED`
- `STATE_SYNCED`

## Payloads principales de juego

`ATTACK_RESOLVED`:

```json
{
  "attacker": "uuid",
  "defender": "uuid",
  "attackingPokemon": "card-instance-id",
  "defendingPokemon": "card-instance-id",
  "damage": 30,
  "baseDamage": 30,
  "requiredEnergy": 1,
  "attachedEnergy": 1,
  "cancelled": false,
  "weaknessMultiplier": 1,
  "resistanceReduction": 0,
  "auditTrail": []
}
```

`KO`:

```json
{
  "attacker": "uuid",
  "defender": "uuid",
  "knockedOutCard": "card-instance-id",
  "promotedCard": "card-instance-id",
  "defenderHasNoPokemon": false
}
```

`PRIZE_TAKEN`:

```json
{
  "player": "uuid",
  "prizesTaken": 1,
  "prizeCardsRemaining": 5
}
```

`GAME_FINISHED`:

```json
{
  "winner": "uuid",
  "reason": "PRIZES"
}
```

Razones posibles actuales:

- `PRIZES`
- `KO_TOTAL`
- `DECK_OUT`
- `UNKNOWN`

## Canal WebSocket/STOMP

Los clientes escuchan:

```text
/topic/games/{gameId}/events
```

## Reconexion

Consultar eventos pendientes:

```text
GET /internal/events/{gameId}?sinceSequence={lastSeenSequence}
```

Respuesta:

```json
{
  "gameId": "uuid",
  "lastSequence": 10,
  "replayFromMemory": true,
  "syncedAt": "2026-05-06T18:05:00Z",
  "events": []
}
```
