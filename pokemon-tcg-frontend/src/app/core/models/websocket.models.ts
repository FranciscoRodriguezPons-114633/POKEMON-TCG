import { WinReason } from './game.models';

export type GameEventType =
  | 'GAME_CREATED'
  | 'PLAYER_JOINED'
  | 'GAME_SETUP'
  | 'SETUP_COMPLETED'
  | 'TURN_STARTED'
  | 'TURN_PHASE_CHANGED'
  | 'CARD_DRAWN'
  | 'ENERGY_ATTACHED'
  | 'SUPPORTER_PLAYED'
  | 'RETREAT_DECLARED'
  | 'ATTACK_RESOLVED'
  | 'KO'
  | 'PRIZE_TAKEN'
  | 'STATUS_APPLIED'
  | 'BETWEEN_TURNS_RESOLVED'
  | 'GAME_FINISHED'
  | 'STATE_SYNCED';

export interface GameEventEnvelope {
  sequence: number;
  schemaVersion: number;
  gameId: string;
  type: GameEventType;
  payload: Record<string, unknown>;
  occurredAt: string;
  receivedAt: string;
}

export interface AttackResolvedPayload {
  attacker: string;
  defender: string;
  attackingPokemon: string;
  defendingPokemon: string;
  damage: number;
  baseDamage: number;
  cancelled: boolean;
  weaknessMultiplier: number;
  resistanceReduction: number;
  auditTrail: string[];
}

export interface KoPayload {
  attacker: string;
  defender: string;
  knockedOutCard: string;
  promotedCard: string | null;
  defenderHasNoPokemon: boolean;
}

export interface PrizeTakenPayload {
  player: string;
  prizesTaken: number;
  prizeCardsRemaining: number;
}

export interface GameFinishedPayload {
  winner: string;
  reason: WinReason;
}
