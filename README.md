# Pokémon TCG Backend - Multi-módulo Maven (3 servicios)

Este repositorio ahora está organizado como **multi-módulo Maven** con 3 servicios independientes:

- `game-service` (puerto 8081)
- `realtime-service` (puerto 8082)
- `card-service` (puerto 8083)

## Estructura

```text
pokemon-tcg-platform/
├── pom.xml (parent)
├── game-service/
├── realtime-service/
└── card-service/
```

## Migraciones realizadas

### 1) Card API a `card-service`

Se migraron responsabilidades de cartas a un servicio dedicado:

- `CardController`
- `CardApiService`
- `PokemonTcgClient`
- Redis cache (TTL 7 días)

### 2) WebSocket a `realtime-service`

Se migró la capa WebSocket STOMP:

- endpoint `/ws`
- broker `/topic`
- endpoint interno `/internal/events` para recibir eventos del `game-service`

### 3) `game-service` con patrones de diseño requeridos

- **State**
  - Estados de partida: `WAITING`, `SETUP`, `ACTIVE`, `FINISHED`
  - Estados de turno: `DRAW`, `MAIN`, `ATTACK`, `BETWEEN_TURNS`
- **Strategy**
  - `TrainerEffectStrategy`
  - `AttackEffectStrategy`
- **Chain of Responsibility**
  - `AttackResolutionPipeline` con 7 pasos:
    1. `EnergyValidationStep`
    2. `ConfusionCheckStep`
    3. `SelectionStep`
    4. `PreAttackStep`
    5. `ModifierStep`
    6. `DamageCalculationStep`
    7. `PostDamageEffectsStep`
- **Observer**
  - `GameEventPublisher` + `GameEventObserver`
  - `HttpRealtimeObserver` notifica al `realtime-service`
- **Repository**
  - Interfaces: `GameStateRepository`, `DeckRepository`, `CardRepository`
  - Implementación inicial: `InMemoryGameStateRepository`
- **Facade**
  - `GameEngineFacade` como API interna del motor

## Endpoints

### game-service

- `POST /api/games`
- `POST /api/games/{gameId}/join`
- `POST /api/games/{gameId}/actions`
- `GET /api/games/{gameId}`

### card-service

- `GET /api/cards?q=set.id:xy1&pageSize=20`
- `GET /api/cards/{id}`

### realtime-service

- `POST /internal/events` (uso interno entre servicios)
- WebSocket STOMP `/ws` + `/topic/games/{gameId}/events`

## Build

```bash
mvn -pl game-service,card-service,realtime-service clean package
```

## Run local

```bash
docker compose up -d
```

> Nota: los Dockerfiles esperan jars construidos en `*/target/`.
