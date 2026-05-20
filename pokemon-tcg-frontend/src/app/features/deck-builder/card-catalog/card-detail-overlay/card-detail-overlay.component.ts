import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input,
  computed,
} from '@angular/core';

import {
  CardSummaryResponse,
  CardAttackResponse,
} from '../../../../core/models/card.models';

@Component({
  selector: 'app-card-detail-overlay',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './card-detail-overlay.component.html',
  styleUrls: ['./card-detail-overlay.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardDetailOverlayComponent {
  @Input({ required: true }) card!: CardSummaryResponse | null;
  @Input({ required: false }) rect: DOMRect | null = null;

  // Compute style for fixed-position overlay based on card bounding rect.
  // Keeps the overlay off-screen safe by clamping to viewport.
  readonly overlayStyle = computed(() => {
    const r = this.rect;
    const width = 360;
    const heightEstimate = 420; // used to prevent overflow when computing top

    if (!r) {
      return {
        left: `calc(50% - ${width / 2}px)`,
        top: `20%`,
      };
    }

    const viewportW = window.innerWidth;
    const viewportH = window.innerHeight;

    // Prefer to position to the right of the card if space available
    let left = r.right + 12;
    if (left + width + 8 > viewportW) {
      left = r.left - width - 12;
    }

    // clamp top
    let top = r.top;
    if (top + heightEstimate + 8 > viewportH) top = Math.max(8, viewportH - heightEstimate - 8);
    if (top < 8) top = 8;

    return {
      left: `${Math.max(8, left)}px`,
      top: `${top}px`,
    };
  });

  imageUrl(card: CardSummaryResponse | null): string {
    if (!card) return '';
    const num = card.id.split('-')[1];
    return `https://images.pokemontcg.io/${card.setId}/${num}.png`;
  }

  // Render cost as colored dots
  costDots(attack: CardAttackResponse): string[] {
    return attack.cost ?? [];
  }

  weaknessesText(): string {
    if (!this.card?.weaknesses?.length) return '';
    return this.card.weaknesses.map((w) => `${w.type} ${w.value}`).join(', ');
  }

  resistancesText(): string {
    if (!this.card?.resistances?.length) return '';
    return this.card.resistances.map((r) => `${r.type} ${r.value}`).join(', ');
  }
}
