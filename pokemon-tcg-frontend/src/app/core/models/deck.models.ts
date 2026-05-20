export interface DeckCardInput {
  cardId: string;
  name: string;
  setId: string | null;
  quantity: number;
  type: string;
  subtype: string | null;
  basicEnergy: boolean;
  basicPokemon: boolean;
  aceSpec: boolean;
  hp: number | null;
  attackDamage: number | null;
  attackRequiredEnergy: number | null;
}

export interface DeckValidationResult {
  valid: boolean;
  errors: string[];
}

export interface DeckResponse {
  id: string;
  playerId: string;
  name: string;
  cards: DeckCardInput[];
  validation: DeckValidationResult;
}

export interface DeckUpsertRequest {
  playerId: string;
  name: string;
  cards: DeckCardInput[];
}
