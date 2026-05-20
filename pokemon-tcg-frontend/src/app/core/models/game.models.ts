export type GameState = 'WAITING' | 'SETUP' | 'ACTIVE' | 'FINISHED';

export type TurnPhase = 'DRAW' | 'MAIN' | 'ATTACK' | 'BETWEEN_TURNS';

export type GameActionType =
  | 'DRAW'
  | 'ATTACH_ENERGY'
  | 'PLAY_SUPPORTER'
  | 'RETREAT'
  | 'ATTACK'
  | 'END_TURN'
  | 'TAKE_PRIZE'
  | 'APPLY_SPECIAL_CONDITION'
  | 'RESOLVE_BETWEEN_TURNS';

export type StatusCondition = 'ASLEEP' | 'PARALYZED' | 'CONFUSED' | 'POISONED' | 'BURNED';

export type WinReason = 'PRIZES' | 'KO_TOTAL' | 'DECK_OUT' | 'UNKNOWN';

export interface CreateGameRequest {
  playerId: string;
  deckId: string;
}

export interface JoinGameRequest {
  playerId: string;
  deckId: string;
}

export interface GameActionRequest {
  actionType: GameActionType;
  condition?: StatusCondition;
}

export interface GameStateResponse {
  gameId: string;
  state: GameState;
  phase: TurnPhase;
  currentTurnPlayer: string;
  firstPlayer: string;
  winner: string | null;
  setupByPlayer: Record<string, PlayerSetupSummary>;
  activePokemonByPlayer: Record<string, unknown>;
  benchByPlayer: Record<string, unknown[]>;
  handSizesByPlayer: Record<string, number>;
  deckCardsRemainingByPlayer: Record<string, number>;
  prizeCardsRemainingByPlayer: Record<string, number>;
  discardPilesByPlayer: Record<string, unknown[]>;
  activeDamageCountersByPlayer: Record<string, number>;
  activeAttachedEnergyByPlayer: Record<string, number>;
  statusByPlayer: Record<string, StatusCondition | null>;
}

export interface PlayerSetupSummary {
  mulligans: number;
  handSize: number;
  benchCount: number;
  prizeCount: number;
  hasActive: boolean;
}

export interface GameSyncResponse {
  state: GameStateResponse;
  actionLog: string[];
  syncedAt: string;
}
