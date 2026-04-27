# Pokémon TCG Backend - Foco en Microservicios, REST, WebSocket y Cartas

Este repo contiene una base de backend para Pokémon TCG orientada a separar responsabilidades por dominios de negocio y habilitar despliegue por servicios.

## Arquitectura objetivo de microservicios

- **game-service**: creación/gestión de partidas, turnos y estado del juego.
- **realtime-gateway**: WebSocket STOMP para acciones en tiempo real y broadcast de estado.
- **card-service**: integración con `pokemontcg.io` + caché Redis.

> Actualmente el código está en un único artefacto Spring Boot, pero ya está separado por capas y responsabilidades para moverlo a servicios independientes sin reescribir lógica core.

## Stack

- Java 21
- Spring Boot 3.4.x
- Spring Web + WebSocket (STOMP)
- Spring Data Redis
- Spring Data JPA
- PostgreSQL + Redis

## API REST disponible

### Game API

- `POST /api/games` crea partida
- `POST /api/games/{gameId}/join` unirse a partida
- `GET /api/games/{gameId}` obtener estado actual
- `POST /api/games/{gameId}/attack` resolver ataque (fase ATTACK)

### Deck API

- `POST /api/decks/validate` valida reglas base del mazo

### Card API (pokemontcg.io)

- `GET /api/cards?q=set.id:xy1&pageSize=20` búsqueda de cartas
- `GET /api/cards/{cardId}` detalle por id

## WebSocket STOMP

### Endpoints

- Handshake: `/ws`
- Envío cliente: `/app/games/{gameId}/action`
- Broadcast servidor: `/topic/games/{gameId}/state`

### Acciones soportadas

Payload de ejemplo:

```json
{
  "playerId": "4b08f8bf-2f18-44f1-b5cb-5faf4e4452d1",
  "type": "END_MAIN"
}
```

`type` puede ser:

- `END_MAIN`
- `ATTACK`
- `NEXT_TURN`

## Card caching (Redis)

Se cachea por 7 días:

- búsquedas: `cards:search:{query}:{pageSize}`
- detalle: `cards:id:{cardId}`

## Correr local

```bash
docker compose up -d
mvn spring-boot:run
```

Variables:

- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`
- `POKEMON_TCG_API_KEY`

## Próximo paso sugerido

Extraer `game-service` y `card-service` como módulos Maven independientes (o repos separados), dejando este repo como integración local y contrato API compartido.
