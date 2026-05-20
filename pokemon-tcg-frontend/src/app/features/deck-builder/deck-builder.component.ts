import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';

import { CardSummaryResponse } from '../../core/models/card.models';
import { DeckCardInput, DeckResponse } from '../../core/models/deck.models';
import { DeckService } from '../../core/services/deck.service';
import { CardCatalogComponent } from './card-catalog/card-catalog.component';
import { DeckEditorComponent, SaveDeckForm } from './deck-editor/deck-editor.component';
import { DeckEntry } from './deck-entry.model';

@Component({
  selector: 'app-deck-builder',
  imports: [CommonModule, CardCatalogComponent, DeckEditorComponent],
  templateUrl: './deck-builder.component.html',
  styleUrl: './deck-builder.component.scss',
})
export class DeckBuilderComponent {
  private static readonly uuidPattern =
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

  private readonly deckService = inject(DeckService);

  readonly entries = signal<DeckEntry[]>([]);
  readonly validationErrors = signal<string[]>([]);
  readonly saveError = signal<string | null>(null);
  readonly saveMessage = signal<string | null>(null);
  readonly saving = signal(false);

  addCard(card: CardSummaryResponse): void {
    if (this.totalCards() >= 60) {
      return;
    }

    this.entries.update((entries) => {
      const existingEntry = entries.find((entry) => entry.card.id === card.id);

      if (existingEntry) {
        return entries.map((entry) =>
          entry.card.id === card.id ? { ...entry, quantity: entry.quantity + 1 } : entry,
        );
      }

      return [...entries, { card, quantity: 1 }];
    });
  }

  increaseQuantity(cardId: string): void {
    if (this.totalCards() >= 60) {
      return;
    }

    this.entries.update((entries) =>
      entries.map((entry) =>
        entry.card.id === cardId ? { ...entry, quantity: entry.quantity + 1 } : entry,
      ),
    );
  }

  decreaseQuantity(cardId: string): void {
    this.entries.update((entries) =>
      entries
        .map((entry) =>
          entry.card.id === cardId ? { ...entry, quantity: entry.quantity - 1 } : entry,
        )
        .filter((entry) => entry.quantity > 0),
    );
  }

  saveDeck(form: SaveDeckForm): void {
    if (!DeckBuilderComponent.uuidPattern.test(form.playerId)) {
      this.validationErrors.set(['El Player ID debe ser un UUID valido.']);
      this.saveError.set('No se puede guardar el mazo: el Player ID no cumple el formato requerido.');
      this.saveMessage.set(null);
      this.saving.set(false);
      return;
    }

    const cards = this.toDeckCards();
    const validationErrors = this.validateDeck(cards);

    if (validationErrors.length > 0) {
      this.validationErrors.set(validationErrors);
      this.saveError.set('No se puede guardar el mazo: no se cumplen las condiciones listadas.');
      this.saveMessage.set(null);
      this.saving.set(false);
      return;
    }

    this.saving.set(true);
    this.saveError.set(null);
    this.saveMessage.set(null);
    this.validationErrors.set([]);

    this.deckService
      .create({
        playerId: form.playerId,
        name: form.name,
        cards,
      })
      .subscribe({
        next: (deck) => this.handleSavedDeck(deck),
        error: (error: unknown) => {
          const message = this.getSaveErrorMessage(error);
          this.saveError.set(`No se pudo guardar el mazo: ${message}`);
          this.validationErrors.set([message]);
          this.saving.set(false);
        },
      });
  }

  private totalCards(): number {
    return this.entries().reduce((total, entry) => total + entry.quantity, 0);
  }

  private handleSavedDeck(deck: DeckResponse): void {
    this.validationErrors.set(deck.validation.valid ? [] : deck.validation.errors);
    this.saveMessage.set(deck.validation.valid ? `Mazo guardado: ${deck.name}` : null);
    this.saveError.set(
      deck.validation.valid
        ? null
        : 'El backend guardo la respuesta, pero marco reglas de mazo incumplidas.',
    );
    this.saving.set(false);
  }

  private toDeckCards(): DeckCardInput[] {
    return this.entries().map((entry) => ({
      cardId: entry.card.id,
      name: entry.card.name,
      setId: entry.card.setId,
      quantity: entry.quantity,
      type: entry.card.types[0] ?? entry.card.supertype,
      subtype: entry.card.subtypes[0] ?? null,
      basicEnergy: this.hasSubtype(entry.card, 'Basic') && this.isSupertype(entry.card, 'Energy'),
      basicPokemon: this.hasSubtype(entry.card, 'Basic') && this.isSupertype(entry.card, 'Pokemon'),
      aceSpec: this.hasSubtype(entry.card, 'ACE SPEC'),
      hp: entry.card.hp,
      attackDamage: this.parseAttackDamage(entry.card),
      attackRequiredEnergy: entry.card.attacks[0]?.convertedEnergyCost ?? null,
    }));
  }

  private validateDeck(cards: DeckCardInput[]): string[] {
    const errors: string[] = [];
    const total = cards.reduce((sum, card) => sum + card.quantity, 0);

    if (total !== 60) {
      errors.push(`El mazo debe tener exactamente 60 cartas. Ahora tiene ${total}.`);
    }

    if (!cards.some((card) => card.basicPokemon)) {
      errors.push('El mazo debe incluir al menos 1 Pokemon Basico.');
    }

    cards
      .filter((card) => !card.basicEnergy && card.quantity > 4)
      .forEach((card) => errors.push(`La carta '${card.name}' excede el maximo de 4 copias.`));

    cards
      .filter((card) => !this.isRequiredSet(card))
      .forEach((card) => errors.push(`La carta '${card.name}' no pertenece al set obligatorio xy1.`));

    const aceSpecCount = cards
      .filter((card) => card.aceSpec)
      .reduce((sum, card) => sum + card.quantity, 0);

    if (aceSpecCount > 1) {
      errors.push('Solo se permite 1 carta AS TACTICO en todo el mazo.');
    }

    return errors;
  }

  private getSaveErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'el navegador no pudo conectarse con game-service; revisa que el backend este levantado y CORS este activo.';
      }

      return this.readBackendMessage(error.error) ?? `game-service respondio HTTP ${error.status}.`;
    }

    return 'game-service rechazo la solicitud, pero no envio un motivo legible.';
  }

  private readBackendMessage(errorBody: unknown): string | null {
    if (this.hasMessage(errorBody)) {
      return errorBody.message;
    }

    if (typeof errorBody === 'string' && errorBody.trim()) {
      return errorBody;
    }

    return null;
  }

  private hasMessage(value: unknown): value is { message: string } {
    return (
      typeof value === 'object' &&
      value !== null &&
      'message' in value &&
      typeof value.message === 'string' &&
      value.message.trim().length > 0
    );
  }

  private parseAttackDamage(card: CardSummaryResponse): number | null {
    const damage = card.attacks[0]?.damage.match(/\d+/)?.[0];
    return damage === undefined ? null : Number(damage);
  }

  private hasSubtype(card: CardSummaryResponse, subtype: string): boolean {
    return card.subtypes.some((cardSubtype) => this.normalize(cardSubtype) === this.normalize(subtype));
  }

  private isSupertype(card: CardSummaryResponse, supertype: string): boolean {
    return this.normalize(card.supertype) === this.normalize(supertype);
  }

  private isRequiredSet(card: DeckCardInput): boolean {
    const setId = card.setId?.trim() || card.cardId.split('-')[0] || '';
    return setId.toLowerCase() === 'xy1';
  }

  private normalize(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();
  }
}
