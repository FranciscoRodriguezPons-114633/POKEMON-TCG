import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DeckResponse, DeckUpsertRequest } from '../models/deck.models';

@Injectable({ providedIn: 'root' })
export class DeckService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.gameServiceUrl}/api/decks`;

  create(request: DeckUpsertRequest): Observable<DeckResponse> {
    return this.http.post<DeckResponse>(this.baseUrl, request);
  }

  update(deckId: string, request: DeckUpsertRequest): Observable<DeckResponse> {
    return this.http.put<DeckResponse>(`${this.baseUrl}/${deckId}`, request);
  }

  get(deckId: string): Observable<DeckResponse> {
    return this.http.get<DeckResponse>(`${this.baseUrl}/${deckId}`);
  }

  delete(deckId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${deckId}`);
  }

  listByPlayer(playerId: string): Observable<DeckResponse[]> {
    const params = new HttpParams().set('playerId', playerId);

    return this.http.get<DeckResponse[]>(this.baseUrl, { params });
  }
}
