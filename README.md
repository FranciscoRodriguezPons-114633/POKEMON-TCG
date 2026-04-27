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
