import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CardSearchResponse, CardSummaryResponse } from '../models/card.models';

@Injectable({ providedIn: 'root' })
export class CardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.cardServiceUrl}/api/cards`;

  // Added optional `page` parameter to support pagination queries
  search(q: string, pageSize: number, page?: number): Observable<CardSearchResponse> {
    let params = new HttpParams().set('q', q).set('pageSize', pageSize);

    if (page !== undefined && page !== null) {
      params = params.set('page', String(page));
    }

    return this.http.get<CardSearchResponse>(this.baseUrl, { params });
  }

  getById(id: string): Observable<CardSummaryResponse> {
    return this.http.get<CardSummaryResponse>(`${this.baseUrl}/${id}`);
  }
}
