import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CardSummaryResponse } from '../../../core/models/card.models';
import { CardService } from '../../../core/services/card.service';

@Component({
  selector: 'app-card-search',
  imports: [CommonModule, FormsModule],
  templateUrl: './card-search.component.html',
  styleUrl: './card-search.component.scss',
})
export class CardSearchComponent {
  private readonly cardService = inject(CardService);

  @Output() readonly cardSelected = new EventEmitter<CardSummaryResponse>();

  readonly cards = signal<CardSummaryResponse[]>([]);
  readonly errorMessage = signal<string | null>(null);
  readonly loading = signal(false);

  query = 'set.id:xy1';
  pageSize = 20;

  search(): void {
    const trimmedQuery = this.query.trim();

    if (!trimmedQuery) {
      this.errorMessage.set('Ingresa un filtro de busqueda.');
      this.cards.set([]);
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.cardService.search(trimmedQuery, this.pageSize).subscribe({
      next: (response) => {
        this.cards.set(response.cards);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('No se pudieron buscar cartas.');
        this.loading.set(false);
      },
    });
  }

  selectCard(card: CardSummaryResponse): void {
    this.cardSelected.emit(card);
  }

  trackByCardId(_index: number, card: CardSummaryResponse): string {
    return card.id;
  }
}
