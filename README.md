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
#### 1. Chain of Responsibility (Pipeline de Ataque)
Se implementó un `AttackResolutionPipeline` que garantiza que el flujo de ataque siga los 7 pasos oficiales del TCG de forma determinista:
1. **EnergyValidationStep**: Verifica que el Pokémon tenga las energías necesarias.
2. **ConfusionCheckStep**: Si el Pokémon está confundido, lanza moneda para ver si falla.
3. **SelectionStep**: Elige el objetivo y el ataque específico.
4. **PreAttackStep**: Aplica efectos que ocurren "antes" del daño (ej: protecciones).
5. **ModifierStep**: Aplica debilidades, resistencias y herramientas aplicadas.
6. **DamageCalculationStep**: Realiza el cálculo matemático final del daño.
7. **PostDamageEffectsStep**: Aplica efectos secundarios (ej: poner contadores de veneno).

#### 2. Observer (Arquitectura de Eventos)
- **GameEventPublisher**: Centraliza todos los cambios de estado del juego.
- **GameEventObserver**: Interfaz para suscriptores internos.
- **HttpRealtimeObserver**: Implementación específica que notifica cambios al `realtime-service` para su difusión vía WebSockets.
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

## Planificación (Fase 0)

Se agregaron artefactos de gestión para congelar alcance y preparar ejecución por historias:

- `docs/phase-0/mvp-scope.md`
- `docs/phase-0/definition-of-done-rf.md`
- `docs/phase-0/product-backlog-stories.md`

## Estado de legacy monolito (`src/` raíz)

La estructura legacy de monolito en `src/` de raíz **no forma parte** de esta versión consolidada.
La fuente activa del backend está únicamente en los módulos:

- `game-service/src`
- `card-service/src`
- `realtime-service/src`

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

# Pokémon TCG Backend (Spring Boot)

Backend base para una implementación digital de Pokémon TCG con arquitectura en capas:

- **Presentation**: REST + WebSocket (STOMP).
- **Application**: servicios de orquestación (`GameService`, `DeckService`).
- **Domain**: entidades y motor (`GameEngineFacade`, validadores de reglas).
- **Infrastructure**: cliente `pokemontcg.io`, JPA, Redis.

## Stack

- Java 21
- Spring Boot 3.4.x
- PostgreSQL
- Redis
- Maven

## Estructura principal

```text
src/main/java/com/utn/pokemontcg
├── config
├── presentation/controller
├── presentation/dto
├── application/service
├── domain/model
├── domain/engine
├── domain/validator
└── infrastructure
    ├── external/pokemontcg
    └── persistence
```

## Endpoints base

- `POST /api/games` crear partida
- `POST /api/games/{gameId}/join` unirse a partida
- `GET /api/games/{gameId}` estado de partida
- `POST /api/games/{gameId}/attack` transición de ataque (placeholder)
- `POST /api/decks/validate` validación de mazo (60 cartas, límite de copias)
- `GET /api/cards?q=set.id:xy1&pageSize=20` búsqueda contra `pokemontcg.io`

## Correr local

```bash
docker compose up -d
mvn spring-boot:run
```

Variables útiles:

- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`
- `POKEMON_TCG_API_KEY`

## Tests

```bash
mvn test
```

## Estado del proyecto

Este commit deja una **base funcional y compilable** para continuar con:

- pipeline completo de ataques,
- estados avanzados de juego,
- persistencia completa del estado,
- autenticación JWT,
- sincronización de eventos por WebSocket por sala/partida,
- integración frontend Angular.

