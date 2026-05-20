import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';

import { environment } from '../../../environments/environment';
import { GameEventEnvelope } from '../models/websocket.models';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private readonly eventSubject = new Subject<GameEventEnvelope>();
  private readonly client = new Client({
    brokerURL: environment.realtimeServiceUrl.replace(/^http/, 'ws') + '/ws',
    reconnectDelay: 5000,
  });

  private subscription: StompSubscription | null = null;

  readonly events$: Observable<GameEventEnvelope> = this.eventSubject.asObservable();

  connect(gameId: string): void {
    this.client.onConnect = () => {
      this.subscription = this.client.subscribe(`/topic/games/${gameId}/events`, (message: IMessage) => {
        const event = JSON.parse(message.body) as GameEventEnvelope;
        this.eventSubject.next(event);
      });
    };

    if (!this.client.active) {
      this.client.activate();
    }
  }

  disconnect(): void {
    this.subscription?.unsubscribe();
    this.subscription = null;
    this.client.deactivate();
  }
}
