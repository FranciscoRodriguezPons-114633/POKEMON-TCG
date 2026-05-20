import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { DeckUpsertRequest } from '../models/deck.models';
import { DeckService } from './deck.service';

describe('DeckService', () => {
  let service: DeckService;
  let http: HttpTestingController;

  const requestBody: DeckUpsertRequest = {
    playerId: 'player-1',
    name: 'Mazo principal',
    cards: [
      {
        cardId: 'xy1-1',
        name: 'Venusaur-EX',
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
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(DeckService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('creates decks using the game-service endpoint', () => {
    service.create(requestBody).subscribe();

    const request = http.expectOne('http://localhost:8081/api/decks');

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(requestBody);
    request.flush({ id: 'deck-1', ...requestBody, validation: { valid: true, errors: [] } });
  });

  it('updates decks using the game-service endpoint', () => {
    service.update('deck-1', requestBody).subscribe();

    const request = http.expectOne('http://localhost:8081/api/decks/deck-1');

    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(requestBody);
    request.flush({ id: 'deck-1', ...requestBody, validation: { valid: true, errors: [] } });
  });

  it('gets decks by id using the game-service endpoint', () => {
    service.get('deck-1').subscribe();

    const request = http.expectOne('http://localhost:8081/api/decks/deck-1');

    expect(request.request.method).toBe('GET');
    request.flush({ id: 'deck-1', ...requestBody, validation: { valid: true, errors: [] } });
  });

  it('deletes decks using the game-service endpoint', () => {
    service.delete('deck-1').subscribe();

    const request = http.expectOne('http://localhost:8081/api/decks/deck-1');

    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('lists decks by player using the game-service endpoint', () => {
    service.listByPlayer('player-1').subscribe();

    const request = http.expectOne((req) => req.url === 'http://localhost:8081/api/decks');

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('playerId')).toBe('player-1');
    request.flush([]);
  });
});
