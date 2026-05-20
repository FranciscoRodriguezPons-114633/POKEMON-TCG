import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CreateGameRequest,
  GameActionRequest,
  GameStateResponse,
  GameSyncResponse,
  JoinGameRequest,
} from '../models/game.models';

@Injectable({ providedIn: 'root' })
export class GameService {
  create(_request: CreateGameRequest): Observable<GameStateResponse> {
    throw new Error('GameService.create is pending implementation in block 3.');
  }

  join(_gameId: string, _request: JoinGameRequest): Observable<GameStateResponse> {
    throw new Error('GameService.join is pending implementation in block 3.');
  }

  setup(_gameId: string, _request: unknown): Observable<GameStateResponse> {
    // TODO: Replace unknown when the backend exposes the exact SetupGameRequest fields.
    throw new Error('GameService.setup is pending implementation.');
  }

  executeAction(_gameId: string, _request: GameActionRequest): Observable<GameStateResponse> {
    throw new Error('GameService.executeAction is pending implementation in block 6.');
  }

  getState(_gameId: string): Observable<GameStateResponse> {
    throw new Error('GameService.getState is pending implementation in block 5.');
  }

  sync(_gameId: string): Observable<GameSyncResponse> {
    throw new Error('GameService.sync is pending implementation in block 4.');
  }

  getLogs(_gameId: string): Observable<{ entries: string[] }> {
    throw new Error('GameService.getLogs is pending implementation.');
  }
}
