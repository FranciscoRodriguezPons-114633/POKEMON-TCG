# Arquitectura de comunicacion entre servicios

## Vista general

```mermaid
flowchart LR
    FE["Frontend\nREST Client\nWebSocket/STOMP Client"]

    GS["game-service\nREST Web Server\nGame source of truth\nSnapshots + action log"]

    RTS["realtime-service\nREST Web Server interno\nWebSocket/STOMP Server\nEvent broadcaster"]

    CS["card-service\nREST Web Server\nREST Client externo\nRedis cache"]

    PG[("Postgres\nGame snapshots\nAction logs\nDecks")]

    REDIS[("Redis\nCard cache")]

    EXT["pokemontcg.io\nExternal REST API"]

    FE -- "REST: commands, state, decks" --> GS
    FE -- "REST: card search/details" --> CS
    FE -- "WebSocket/STOMP: listens game events" --> RTS

    GS -- "JPA/Hibernate/JDBC" --> PG
    GS -- "REST: publishes game events" --> RTS

    RTS -- "WebSocket/STOMP: broadcasts events" --> FE

    CS -- "REST: fetches cards" --> EXT
    CS -- "Redis cache read/write" --> REDIS
```

## Flujo de una accion de juego

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant GS as game-service
    participant PG as Postgres
    participant RTS as realtime-service

    FE->>GS: REST action/create/join/setup
    GS->>GS: Validate rules and update game
    GS->>PG: Save snapshot + action log
    GS->>RTS: REST publish game event
    RTS->>RTS: Store event in memory
    RTS-->>FE: WebSocket/STOMP event
    GS-->>FE: REST response with current state
```

## Resumen

- `game-service` es la fuente de verdad del juego.
- `realtime-service` distribuye eventos, pero no modifica partidas.
- `card-service` consulta cartas externas y cachea resultados.
- El frontend deberia usar REST para comandos/estado y WebSocket/STOMP para eventos en tiempo real.
