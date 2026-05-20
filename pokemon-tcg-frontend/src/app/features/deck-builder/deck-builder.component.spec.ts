import { HttpErrorResponse } from '@angular/common/http';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { CardSummaryResponse } from '../../core/models/card.models';
import { DeckResponse, DeckUpsertRequest } from '../../core/models/deck.models';
import { DeckService } from '../../core/services/deck.service';
import { DeckBuilderComponent } from './deck-builder.component';

describe('DeckBuilderComponent', () => {
  const card: CardSummaryResponse = {
    id: 'xy1-1',
    name: 'Venusaur-EX',
    setId: 'xy1',
    supertype: 'Pokemon',
    subtypes: ['Basic', 'EX'],
    types: ['Grass'],
    hp: 180,
    attacks: [{ name: 'Poison Powder', text: '', damage: '60', cost: ['Grass'], convertedEnergyCost: 3 }],
    weaknesses: [],
    resistances: [],
  };

  let deckService: Pick<DeckService, 'create'>;
  let createDeck: ReturnType<typeof vi.fn<(request: DeckUpsertRequest) => ReturnType<DeckService['create']>>>;

  beforeEach(async () => {
    createDeck = vi.fn();
    deckService = { create: createDeck };

    await TestBed.configureTestingModule({
      imports: [DeckBuilderComponent],
      providers: [provideHttpClient(), { provide: DeckService, useValue: deckService }],
    }).compileComponents();
  });

  it('adds and removes cards from the deck', () => {
    const fixture = TestBed.createComponent(DeckBuilderComponent);
    const component = fixture.componentInstance;

    component.addCard(card);
    component.addCard(card);
    component.decreaseQuantity(card.id);

    expect(component.entries()).toEqual([{ card, quantity: 1 }]);
  });

  it('shows local deck rules when the deck cannot be saved', () => {
    const fixture = TestBed.createComponent(DeckBuilderComponent);
    const component = fixture.componentInstance;

    component.addCard(card);
    component.saveDeck({ playerId: '11111111-1111-4111-8111-111111111111', name: 'Mazo principal' });

    expect(createDeck).not.toHaveBeenCalled();
    expect(component.saveError()).toBe(
      'No se puede guardar el mazo: no se cumplen las condiciones listadas.',
    );
    expect(component.validationErrors()).toContain('El mazo debe tener exactamente 60 cartas. Ahora tiene 1.');
  });

  it('shows a local error when player id is not a uuid', () => {
    const fixture = TestBed.createComponent(DeckBuilderComponent);
    const component = fixture.componentInstance;
    component.entries.set(validEntries());

    component.saveDeck({ playerId: 'player-1', name: 'Mazo principal' });

    expect(createDeck).not.toHaveBeenCalled();
    expect(component.saveError()).toBe(
      'No se puede guardar el mazo: el Player ID no cumple el formato requerido.',
    );
    expect(component.validationErrors()).toEqual(['El Player ID debe ser un UUID valido.']);
  });

  it('sends card metadata when saving a valid deck', () => {
    const fixture = TestBed.createComponent(DeckBuilderComponent);
    const component = fixture.componentInstance;
    component.entries.set(validEntries());
    createDeck.mockReturnValue(
      of({
        id: 'deck-1',
        playerId: 'player-1',
        name: 'Mazo principal',
        cards: [
          {
            cardId: 'xy1-1',
            name: 'Card 1',
            setId: 'xy1',
            quantity: 4,
            type: 'Grass',
            subtype: 'Basic',
            basicEnergy: false,
            basicPokemon: true,
            aceSpec: false,
            hp: 180,
            attackDamage: 60,
            attackRequiredEnergy: 3,
          },
        ],
        validation: { valid: true, errors: [] },
      } satisfies DeckResponse),
    );

    component.saveDeck({ playerId: '11111111-1111-4111-8111-111111111111', name: 'Mazo principal' });

    expect(createDeck).toHaveBeenCalledWith(
      expect.objectContaining({
      playerId: '11111111-1111-4111-8111-111111111111',
      name: 'Mazo principal',
        cards: expect.arrayContaining([
          expect.objectContaining({
            cardId: 'xy1-1',
            name: 'Card 1',
            setId: 'xy1',
            quantity: 4,
            type: 'Grass',
            subtype: 'Basic',
            basicEnergy: false,
            basicPokemon: true,
            aceSpec: false,
            hp: 180,
            attackDamage: 60,
            attackRequiredEnergy: 3,
          }),
        ]),
      }),
    );
    expect(component.saveMessage()).toBe('Mazo guardado: Mazo principal');
  });

  it('shows the backend condition when the deck cannot be saved', () => {
    const fixture = TestBed.createComponent(DeckBuilderComponent);
    const component = fixture.componentInstance;
    component.entries.set(validEntries());
    createDeck.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { message: 'El mazo debe tener exactamente 60 cartas.' },
          }),
      ),
    );

    component.saveDeck({ playerId: '11111111-1111-4111-8111-111111111111', name: 'Mazo principal' });

    expect(component.saveError()).toBe(
      'No se pudo guardar el mazo: El mazo debe tener exactamente 60 cartas.',
    );
    expect(component.validationErrors()).toEqual(['El mazo debe tener exactamente 60 cartas.']);
    expect(component.saving()).toBe(false);
  });

  function validEntries() {
    return Array.from({ length: 15 }, (_, index) => ({
      card: {
        ...card,
        id: `xy1-${index + 1}`,
        name: `Card ${index + 1}`,
      },
      quantity: 4,
    }));
  }
});
