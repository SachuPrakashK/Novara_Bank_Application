import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import type { CardStatus } from '../../core/cards/card.models';
@Component({
  selector: 'novara-card-status',
  template: `<span [attr.data-status]="status()">{{ status().replace('_', ' ') }}</span>`,
  styles: [
    `
      span {
        display: inline-flex;
        padding: 0.3rem 0.65rem;
        border-radius: 999px;
        background: var(--novara-success-soft);
        color: var(--novara-success);
        font-size: 0.72rem;
        font-weight: 700;
        letter-spacing: 0.04em;
      }
      span[data-status='FROZEN'],
      span[data-status='BLOCKED'] {
        background: var(--novara-warning-soft);
        color: var(--novara-warning);
      }
      span[data-status='CLOSED'],
      span[data-status='EXPIRED'] {
        background: var(--novara-bg-subtle);
        color: var(--novara-text-secondary);
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardStatusBadgeComponent {
  readonly status = input.required<CardStatus>();
}
