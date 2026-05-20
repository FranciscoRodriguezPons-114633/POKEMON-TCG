import { CardSummaryResponse } from '../../core/models/card.models';

export interface DeckEntry {
  card: CardSummaryResponse;
  quantity: number;
}
