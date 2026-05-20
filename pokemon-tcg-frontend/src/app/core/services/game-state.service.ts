import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

import { GameStateResponse } from '../models/game.models';

@Injectable({ providedIn: 'root' })
export class GameStateService {
  private readonly stateSubject = new BehaviorSubject<GameStateResponse | null>(null);

  readonly state$: Observable<GameStateResponse | null> = this.stateSubject.asObservable();

  setState(state: GameStateResponse): void {
    this.stateSubject.next(state);
  }

  clearState(): void {
    this.stateSubject.next(null);
  }
}
