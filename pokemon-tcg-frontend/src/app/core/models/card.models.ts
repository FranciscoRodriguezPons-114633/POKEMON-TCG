export interface TypeModifierResponse {
  type: string;
  value: string;
}

export interface CardAttackResponse {
  name: string;
  text: string;
  damage: string;
  cost: string[];
  convertedEnergyCost: number;
}

export interface CardSummaryResponse {
  id: string;
  name: string;
  setId: string;
  supertype: string;
  subtypes: string[];
  types: string[];
  hp: number | null;
  attacks: CardAttackResponse[];
  weaknesses: TypeModifierResponse[];
  resistances: TypeModifierResponse[];
}

export interface CardSearchResponse {
  query: string;
  pageSize: number;
  count: number;
  totalCount: number;
  cards: CardSummaryResponse[];
}
