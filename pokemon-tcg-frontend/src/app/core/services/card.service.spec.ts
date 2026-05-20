import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { CardService } from './card.service';

describe('CardService', () => {
  let service: CardService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(CardService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('searches cards using the card-service endpoint', () => {
    service.search('set.id:xy1', 20).subscribe();

    const request = http.expectOne((req) => req.url === 'http://localhost:8083/api/cards');

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('q')).toBe('set.id:xy1');
    expect(request.request.params.get('pageSize')).toBe('20');
    request.flush({ query: 'set.id:xy1', pageSize: 20, count: 0, totalCount: 0, cards: [] });
  });

  it('gets a card by id using the card-service endpoint', () => {
    service.getById('xy1-1').subscribe();

    const request = http.expectOne('http://localhost:8083/api/cards/xy1-1');

    expect(request.request.method).toBe('GET');
    request.flush({
      id: 'xy1-1',
      name: 'Venusaur-EX',
      setId: 'xy1',
      supertype: 'Pokemon',
      subtypes: [],
      types: [],
      hp: 180,
      attacks: [],
      weaknesses: [],
      resistances: [],
    });
  });
});
