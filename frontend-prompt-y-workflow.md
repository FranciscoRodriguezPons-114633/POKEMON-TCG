# Frontend – Prompt Ajustado al Backend Real + Workflow de Ataque

---

## PROMPT PARA EL FRONTEND

```
Sos un desarrollador frontend senior especializado en Angular 21+ con TypeScript estricto.
Estás construyendo el frontend de una versión digital del Pokémon TCG para un proyecto
universitario (UTN FRC – Programación III).

El backend ya existe y está en producción local. Es una arquitectura de TRES microservicios:

| Servicio         | Puerto | Responsabilidad                                      |
|------------------|--------|------------------------------------------------------|
| game-service     | 8081   | Fuente de verdad. REST: comandos + estado del juego  |
| realtime-service | 8082   | WebSocket/STOMP. Broadcast de eventos en tiempo real |
| card-service     | 8083   | REST. Búsqueda y detalle de cartas (cache Redis)     |

El frontend tiene TRES responsabilidades:
1. Enviar comandos REST a game-service (acciones de juego, mazos)
2. Buscar cartas vía REST a card-service (Deck Builder)
3. Escuchar eventos en tiempo real vía WebSocket/STOMP desde realtime-service

El frontend NO toma decisiones de juego. Solo presenta lo que el servidor indica.

---

## ENDPOINTS DEL BACKEND (exactos, no inventar)

### game-service (http://localhost:8081)

#### Juego
POST   /api/games                        → CreateGameRequest { playerId: UUID, deckId: UUID }
POST   /api/games/{gameId}/join          → JoinGameRequest   { playerId: UUID, deckId: UUID }
POST   /api/games/{gameId}/setup         → SetupGameRequest  (ver nota*)
POST   /api/games/{gameId}/actions       → GameActionRequest { actionType: GameActionType, condition?: StatusCondition }
GET    /api/games/{gameId}               → GameStateResponse
GET    /api/games/{gameId}/sync          → GameSyncResponse  { state, actionLog, syncedAt }
GET    /api/games/{gameId}/logs          → { entries: string[] }

#### Mazos
POST   /api/decks                        → DeckUpsertRequest { playerId, name, cards: DeckCardInput[] }
PUT    /api/decks/{deckId}               → DeckUpsertRequest
GET    /api/decks/{deckId}               → DeckResponse
DELETE /api/decks/{deckId}              → 204
GET    /api/decks?playerId={uuid}        → DeckResponse[]

### card-service (http://localhost:8083)

GET    /api/cards?q=set.id:xy1&pageSize=20  → CardSearchResponse
GET    /api/cards/{id}                       → CardSummaryResponse

---

## TIPOS TYPESCRIPT EXACTOS (derivados del backend)

```typescript
// ── Enums ────────────────────────────────────────────────────────────────
export type GameState = 'WAITING' | 'SETUP' | 'ACTIVE' | 'FINISHED';
export type TurnPhase = 'DRAW' | 'MAIN' | 'ATTACK' | 'BETWEEN_TURNS';
export type GameActionType =
  | 'DRAW' | 'ATTACH_ENERGY' | 'PLAY_SUPPORTER'
  | 'RETREAT' | 'ATTACK' | 'END_TURN'
  | 'TAKE_PRIZE' | 'APPLY_SPECIAL_CONDITION' | 'RESOLVE_BETWEEN_TURNS';
export type StatusCondition = 'ASLEEP' | 'PARALYZED' | 'CONFUSED' | 'POISONED' | 'BURNED';
export type WinReason = 'PRIZES' | 'KO_TOTAL' | 'DECK_OUT' | 'UNKNOWN';

// ── DTOs de Cartas ────────────────────────────────────────────────────────
export interface TypeModifierResponse { type: string; value: string; }
export interface CardAttackResponse {
  name: string; text: string; damage: string;
  cost: string[]; convertedEnergyCost: number;
}
export interface CardSummaryResponse {
  id: string; name: string; setId: string;
  supertype: string; subtypes: string[]; types: string[];
  hp: number | null; attacks: CardAttackResponse[];
  weaknesses: TypeModifierResponse[]; resistances: TypeModifierResponse[];
}
export interface CardSearchResponse {
  query: string; pageSize: number; count: number;
  totalCount: number; cards: CardSummaryResponse[];
}

// ── DTOs de Mazos ─────────────────────────────────────────────────────────
export interface DeckCardInput { cardId: string; quantity: number; }
export interface DeckValidationResult { valid: boolean; errors: string[]; }
export interface DeckResponse {
  id: string; playerId: string; name: string;
  cards: DeckCardInput[]; validation: DeckValidationResult;
}
export interface DeckUpsertRequest { playerId: string; name: string; cards: DeckCardInput[]; }

// ── DTOs de Juego ─────────────────────────────────────────────────────────
export interface CreateGameRequest { playerId: string; deckId: string; }
export interface JoinGameRequest   { playerId: string; deckId: string; }
export interface GameActionRequest { actionType: GameActionType; condition?: StatusCondition; }

export interface GameStateResponse {
  gameId: string;
  state: GameState;
  phase: TurnPhase;
  currentTurnPlayer: string;
  firstPlayer: string;
  winner: string | null;
  setupByPlayer:              Record<string, PlayerSetupSummary>;
  activePokemonByPlayer:      Record<string, unknown>;
  benchByPlayer:              Record<string, unknown[]>;
  handSizesByPlayer:          Record<string, number>;
  deckCardsRemainingByPlayer: Record<string, number>;
  prizeCardsRemainingByPlayer:Record<string, number>;
  discardPilesByPlayer:       Record<string, unknown[]>;
  activeDamageCountersByPlayer:Record<string, number>;
  activeAttachedEnergyByPlayer:Record<string, number>;
  statusByPlayer:             Record<string, StatusCondition | null>;
}
export interface PlayerSetupSummary {
  mulligans: number; handSize: number; benchCount: number;
  prizeCount: number; hasActive: boolean;
}
export interface GameSyncResponse {
  state: GameStateResponse; actionLog: string[]; syncedAt: string;
}

// ── WebSocket / STOMP ─────────────────────────────────────────────────────
export type GameEventType =
  | 'GAME_CREATED' | 'PLAYER_JOINED' | 'GAME_SETUP' | 'SETUP_COMPLETED'
  | 'TURN_STARTED' | 'TURN_PHASE_CHANGED' | 'CARD_DRAWN'
  | 'ENERGY_ATTACHED' | 'SUPPORTER_PLAYED' | 'RETREAT_DECLARED'
  | 'ATTACK_RESOLVED' | 'KO' | 'PRIZE_TAKEN'
  | 'STATUS_APPLIED' | 'BETWEEN_TURNS_RESOLVED'
  | 'GAME_FINISHED' | 'STATE_SYNCED';

export interface GameEventEnvelope {
  sequence: number;
  schemaVersion: number;
  gameId: string;
  type: GameEventType;
  payload: Record<string, unknown>;
  occurredAt: string;
  receivedAt: string;
}

// Payloads tipados de los eventos más importantes
export interface AttackResolvedPayload {
  attacker: string; defender: string;
  attackingPokemon: string; defendingPokemon: string;
  damage: number; baseDamage: number;
  cancelled: boolean; weaknessMultiplier: number;
  resistanceReduction: number; auditTrail: string[];
}
export interface KoPayload {
  attacker: string; defender: string;
  knockedOutCard: string; promotedCard: string | null;
  defenderHasNoPokemon: boolean;
}
export interface PrizeTakenPayload {
  player: string; prizesTaken: number; prizeCardsRemaining: number;
}
export interface GameFinishedPayload { winner: string; reason: WinReason; }
```

---

## WEBSOCKET – CONEXIÓN STOMP

El realtime-service corre en puerto 8082 y usa STOMP sobre WebSocket.

- Endpoint de handshake: `ws://localhost:8082/ws`
- Topic a suscribir por partida: `/topic/games/{gameId}/events`
- Recuperación tras desconexión:
  1. GET `/api/games/{gameId}/sync` en game-service → rehidratar estado
  2. GET `http://localhost:8082/internal/events/{gameId}?sinceSequence={lastSeen}` → replay de eventos perdidos

Usar `@stomp/stompjs` (no SockJS) para la conexión.

---

## ARQUITECTURA ANGULAR ESPERADA

```
src/app/
├── core/
│   ├── models/                  ← todos los interfaces TypeScript del bloque anterior
│   │   ├── card.models.ts
│   │   ├── deck.models.ts
│   │   ├── game.models.ts
│   │   └── websocket.models.ts
│   └── services/
│       ├── card.service.ts      ← GET /api/cards (card-service:8083)
│       ├── deck.service.ts      ← CRUD /api/decks (game-service:8081)
│       ├── game.service.ts      ← REST /api/games (game-service:8081)
│       ├── game-state.service.ts← BehaviorSubject con el GameStateResponse actual
│       └── websocket.service.ts ← STOMP client, Subject<GameEventEnvelope>
├── features/
│   ├── deck-builder/
│   │   ├── deck-builder.component.ts
│   │   ├── card-search/
│   │   └── deck-editor/
│   ├── lobby/
│   │   └── lobby.component.ts
│   └── game/
│       ├── board/
│       │   ├── board.component.ts          ← layout principal
│       │   ├── player-zone/                ← zona activo + banca + premios + descarte
│       │   ├── opponent-zone/              ← ídem pero info limitada
│       │   └── stadium-zone/               ← zona compartida
│       ├── hand/
│       │   └── hand.component.ts           ← drag source
│       ├── action-panel/
│       │   └── action-panel.component.ts   ← botones habilitados por fase
│       └── game-log/
│           └── game-log.component.ts       ← historial de eventos
└── shared/
    ├── components/
    │   ├── card-display/        ← renderiza una CardSummaryResponse
    │   ├── hp-bar/
    │   ├── status-badge/        ← ASLEEP/POISONED/etc con ícono
    │   └── prize-counter/
    └── pipes/
```

Todos los componentes son **standalone**. Change detection **OnPush** en board y zonas.

---

## REGLAS DE PRESENTACIÓN (lo que el frontend DEBE hacer)

### Panel de acciones — habilitación por fase y flags
| Botón            | Habilitado cuando...                                                   |
|------------------|------------------------------------------------------------------------|
| Unir Energía     | `phase === 'MAIN'` y el servidor no ha marcado energía ya adjuntada    |
| Jugar Partidario | `phase === 'MAIN'` y hay Partidarios en mano                           |
| Retirar          | `phase === 'MAIN'` y no se retiró en este turno                        |
| Atacar           | `phase === 'ATTACK'` y Pokémon Activo tiene energía suficiente         |
| Finalizar Turno  | `phase === 'MAIN'` o `phase === 'ATTACK'`                              |

### Información oculta (NUNCA mostrar al cliente local)
- Contenido de la mano del oponente → solo `handSizesByPlayer[opponentId]`
- Orden del mazo → solo `deckCardsRemainingByPlayer[playerId]`
- Contenido de cartas de Premio → solo `prizeCardsRemainingByPlayer[playerId]`

### Condiciones especiales — representación visual
- ASLEEP, CONFUSED, PARALYZED → rotar la carta 90°
- BURNED → ícono de llama sobre la carta
- POISONED → ícono de veneno sobre la carta

### Reconexión
Al detectar desconexión del WebSocket:
1. Llamar `GET /api/games/{gameId}/sync` → actualizar GameStateService
2. Llamar `GET http://localhost:8082/internal/events/{gameId}?sinceSequence={lastSeen}`
3. Procesar eventos pendientes en orden por `sequence`
4. Si no hay eventos (buffer reiniciado), el estado del paso 1 es suficiente

---

## INSTRUCCIONES DE TRABAJO

Cuando implementes algo de este frontend:
1. Indicá qué componente/servicio creás o modificás
2. Código TypeScript completo, tipado estricto, sin `any`
3. Si la lógica de presentación depende de `phase` o `state`, usá los enums exactos del backend
4. No inventes endpoints ni campos en los DTOs — usá exactamente los listados arriba
5. Si necesitás un dato que el backend no expone todavía, marcalo con un TODO comentado
6. Usá `inject()` en vez de constructor injection (Angular 14+)
7. Para el WebSocket, usá `@stomp/stompjs` directamente — no SockJS

---

## STACK

- Angular 21+ standalone components
- TypeScript strict mode
- RxJS para streams y estado reactivo
- Angular CDK Drag & Drop
- `@stomp/stompjs` para WebSocket/STOMP
- Signals de Angular para estado local de componentes donde aplique
```

---

## WORKFLOW — Por dónde arrancar ahora

La idea es ir de lo más simple a lo más complejo, siempre teniendo algo
funcional al final de cada bloque.

---

### BLOQUE 1 — Fundación (hacer esto primero, sin saltear)

**Objetivo:** tener el proyecto Angular creado con la estructura correcta y
los modelos tipados listos. Nada de lógica todavía.

1. Crear proyecto Angular 21+ con `ng new pokemon-tcg-frontend --standalone --strict`
2. Instalar dependencias: `@stomp/stompjs`, `@angular/cdk`
3. Crear `environments/environment.ts` con las URLs de los tres servicios:
   ```ts
   export const environment = {
     gameServiceUrl:     'http://localhost:8081',
     realtimeServiceUrl: 'http://localhost:8082',
     cardServiceUrl:     'http://localhost:8083',
   };
   ```
4. Crear todos los archivos de modelos en `core/models/` copiando los tipos
   del bloque de tipos TypeScript del prompt de arriba.
5. Crear los tres servicios HTTP vacíos (solo los métodos, sin lógica):
   `card.service.ts`, `deck.service.ts`, `game.service.ts`
6. Crear `game-state.service.ts` con un `BehaviorSubject<GameStateResponse | null>`
7. Crear `websocket.service.ts` con la conexión STOMP y un
   `Subject<GameEventEnvelope>` expuesto como Observable

**Checkpoint:** el proyecto compila sin errores. Los servicios están inyectables.

---

### BLOQUE 2 — Deck Builder

**Objetivo:** poder buscar cartas y armar un mazo válido. No requiere
WebSocket ni estado de juego.

1. Feature `deck-builder` con ruta `/deck-builder`
2. `CardService.search(q, pageSize)` → `GET /api/cards`
3. `CardService.getById(id)` → `GET /api/cards/{id}`
4. `DeckService.create/update/get/delete/listByPlayer` → `CRUD /api/decks`
5. Componente de búsqueda con input de filtro y grid de resultados
6. Componente de editor de mazo: lista actual + contador (0/60)
7. Mostrar `DeckValidationResult.errors` cuando `valid === false`
8. Guardar mazo → `POST /api/decks`

**Checkpoint:** se puede crear y guardar un mazo de 60 cartas contra el backend real.

---

### BLOQUE 3 — Lobby

**Objetivo:** poder crear una partida y unirse a una existente.

1. Feature `lobby` con ruta `/lobby`
2. `GameService.create(playerId, deckId)` → `POST /api/games`
3. `GameService.join(gameId, playerId, deckId)` → `POST /api/games/{gameId}/join`
4. Lista de partidas en estado WAITING (puede ser estática por ahora, sin polling)
5. Selector de mazo guardado antes de confirmar
6. Al crear/unirse exitosamente, navegar a `/game/{gameId}`

**Checkpoint:** dos tabs del browser pueden crear y unirse a una partida.

---

### BLOQUE 4 — WebSocket y sincronización de estado

**Objetivo:** el frontend recibe eventos en tiempo real antes de construir el tablero.

1. Implementar `WebSocketService`:
   - Conectar a `ws://localhost:8082/ws` con STOMP
   - `connect(gameId)` → suscribirse a `/topic/games/{gameId}/events`
   - Emitir cada `GameEventEnvelope` recibido por un `Subject`
   - `disconnect()` limpia la suscripción
2. Implementar reconexión automática:
   - Al detectar cierre, llamar `GET /api/games/{gameId}/sync`
   - Luego `GET /internal/events/{gameId}?sinceSequence={lastSeen}`
   - Actualizar `GameStateService` con el resultado
3. `GameStateService` se suscribe al `WebSocketService` y actualiza su
   `BehaviorSubject` ante cada evento relevante

**Checkpoint:** abrir la consola y ver eventos llegando por WebSocket al ejecutar
una acción desde el backend (o desde otro tab).

---

### BLOQUE 5 — Tablero de juego

**Objetivo:** mostrar el estado del juego en pantalla, sin drag & drop todavía.

1. Feature `game` con ruta `/game/:gameId`
2. Al cargar la ruta, llamar `GET /api/games/{gameId}/sync` para poblar estado inicial
3. Layout del board:
   - Zona oponente (arriba): Activo, Banca, cantidad de cartas en mano, premios
   - Zona compartida (centro): estadio activo si lo hay
   - Zona jugador (abajo): ídem pero con contenido real
   - Mano del jugador (abajo del todo): sus cartas
4. Componente `hp-bar` usando `activeDamageCountersByPlayer` vs HP de la carta
5. Componente `status-badge` usando `statusByPlayer`
6. Componente `prize-counter` usando `prizeCardsRemainingByPlayer`
7. Contador de cartas en mazo y descarte
8. `game-log` lateral mostrando `actionLog` del sync

**Checkpoint:** el tablero refleja el estado real del juego al cargar la página.

---

### BLOQUE 6 — Panel de acciones y acciones básicas

**Objetivo:** poder jugar un turno completo desde el frontend.

1. Componente `action-panel` con botones habilitados según la tabla de reglas del prompt
2. Cada botón llama `GameService.executeAction(gameId, actionType)`:
   `POST /api/games/{gameId}/actions`
3. Tras cada respuesta exitosa, actualizar `GameStateService` con el nuevo estado
4. Implementar acciones en orden de complejidad:
   - END_TURN (la más simple, siempre disponible en MAIN/ATTACK)
   - DRAW (solo en fase DRAW)
   - ATTACH_ENERGY (con selector de Pokémon destino)
   - PLAY_SUPPORTER (con selector de carta)
   - RETREAT
   - ATTACK (con selector de ataque del Pokémon Activo)
5. Mostrar notificaciones visuales ante eventos KO, PRIZE_TAKEN, GAME_FINISHED

**Checkpoint:** se puede jugar un turno completo de inicio a fin.

---

### BLOQUE 7 — Drag & Drop (mejora de UX)

**Objetivo:** reemplazar los selectores por interacciones drag & drop con Angular CDK.

1. Arrastrar Pokémon Básico desde mano → zona de Banca (resaltar targets válidos)
2. Arrastrar Energía → cualquier Pokémon propio en juego
3. Arrastrar carta de Entrenador → zona de juego
4. Al soltar en un target válido, ejecutar la acción correspondiente via REST

**Checkpoint:** la experiencia de juego es fluida sin usar botones para colocar cartas.

---

### BLOQUE 8 — Pulido y opcionales

Solo atacar esto si los bloques 1-6 están completos y funcionando.

- Animaciones para KO, evolución, ataques (CSS o Angular Animations)
- Chat en partida (si el backend lo soporta)
- Tests E2E mínimos: crear mazo → unirse a partida → ejecutar un turno

---

## RESUMEN VISUAL DEL ORDEN

```
[1] Fundación → modelos + servicios vacíos
      ↓
[2] Deck Builder → buscar cartas + armar mazo
      ↓
[3] Lobby → crear/unirse a partida
      ↓
[4] WebSocket → recibir eventos en tiempo real
      ↓
[5] Tablero → mostrar estado del juego
      ↓
[6] Acciones → jugar un turno completo
      ↓
[7] Drag & Drop → mejorar la UX
      ↓
[8] Pulido → animaciones, tests E2E
```

Cada bloque es independiente y entregable. Si algo del backend no está
listo, podés mockear la respuesta con un JSON fijo y avanzar igual.
