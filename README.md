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

<<<<<<< HEAD
=======

>>>>>>> origin/codex/generate-complete-backend-code-structure-lr2t6d
## Planificación (Fase 0)

Se agregaron artefactos de gestión para congelar alcance y preparar ejecución por historias:

- `docs/phase-0/mvp-scope.md`
- `docs/phase-0/definition-of-done-rf.md`
- `docs/phase-0/product-backlog-stories.md`

<<<<<<< HEAD
=======

>>>>>>> origin/codex/generate-complete-backend-code-structure-lr2t6d
## Estado de legacy monolito (`src/` raíz)

La estructura legacy de monolito en `src/` de raíz **no forma parte** de esta versión consolidada.
La fuente activa del backend está únicamente en los módulos:

- `game-service/src`
- `card-service/src`
- `realtime-service/src`

<<<<<<< HEAD
=======

>>>>>>> origin/codex/generate-complete-backend-code-structure-lr2t6d
## Persistencia de estado (Fase 1)

- `game-service` ahora persiste snapshots de partida y action log en PostgreSQL vía JPA.
- Repositorio en memoria queda sólo para tests/local profile `memory`.
- La estructura legacy de monolito en `/src` no se usa en esta arquitectura consolidada.


## Estado actual del proyecto

### Hecho hasta ahora
- Arquitectura multi-módulo con `game-service`, `card-service` y `realtime-service`.
- Persistencia base en PostgreSQL para snapshots y logs de partida en `game-service`.
- RF-04 backend inicial: CRUD de mazos (`/api/decks`) y validación oficial mínima (60 cartas, máximo 4 copias salvo energía básica, 1 AS TÁCTICO, al menos 1 Básico).
- Endpoints de juego para create/join/setup/actions/state/logs.

### Falta (prioridad real)
1. RF-01 completo: KO total de campo, condiciones especiales completas y muerte súbita robusta.
2. RF-05 producción: rehidratación y replay robustos con consistencia de concurrencia.
3. RF-06: reconexión robusta por sesión y sincronización de estado pendiente.
4. RF-07 frontend: lobby/tablero/drag&drop/log visual/e2e.
5. RNF calidad: cobertura objetivo (80% global, >90% críticos) y test E2E completo.
