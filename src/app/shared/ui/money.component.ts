import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import type { Money } from '../../core/models/banking.models';
import { formatMoney } from '../../core/utils/formatting';
@Component({ selector: 'novara-money', template: `{{ hidden() ? '••••••' : formatted() }}`, host: { class: 'novara-money' }, changeDetection: ChangeDetectionStrategy.OnPush })
export class MoneyComponent { readonly value = input<Money>(); readonly minor = input<number>(); readonly currency = input<'INR'>('INR'); readonly hidden = input(false); protected readonly formatted = computed(() => formatMoney(this.value() ?? { amountMinor: this.minor() ?? 0, currency: this.currency() })); }
