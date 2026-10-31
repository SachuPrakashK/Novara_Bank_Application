import { AsyncPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, map, of, startWith, switchMap } from 'rxjs';
import { PAYMENTS_API } from '../../core/payments/payments-api';
import { IconComponent } from '../../shared/ui/icon.component';
import { MoneyComponent } from '../../shared/ui/money.component';

@Component({
  selector: 'app-payment-detail',
  imports: [AsyncPipe, DatePipe, RouterLink, IconComponent, MoneyComponent],
  template: `
    @if (vm$ | async; as vm) {
      @if (vm.loading) {
        <section class="state" role="status"><p>Loading payment details…</p></section>
      } @else if (vm.error) {
        <section class="state" role="alert">
          <novara-icon name="alert" />
          <h2>Payment details unavailable</h2>
          <p>The requested payment could not be found or is no longer available.</p>
          <a class="button secondary" routerLink="/payments/history">Return to payment history</a>
        </section>
      } @else if (vm.payment; as p) {
        <header class="premium-header">
          <div>
            <a routerLink="/payments/history" class="eyebrow">← PAYMENT HISTORY</a>
            <h1>{{ p.beneficiaryName }}</h1>
            <p>{{ p.status }} · {{ p.reference || 'Awaiting reference' }}</p>
          </div>
          @if (p.status === 'COMPLETED') {
            <a class="button secondary" [href]="'/api/v1/payments/' + p.quoteId + '/receipt'" target="_blank" rel="noopener">
              <novara-icon name="receipt" />Receipt
            </a>
          }
        </header>
        <div class="detail-grid">
          <section class="detail-card">
            <novara-money [minor]="p.amount.amountMinor" currency="INR" />
            <dl>
              <div><dt>From</dt><dd>{{ p.sourceLabel }}<small>{{ p.sourceMaskedNumber }}</small></dd></div>
              <div><dt>To</dt><dd>{{ p.beneficiaryName }}<small>{{ p.beneficiaryMaskedNumber }}</small></dd></div>
              <div><dt>Method</dt><dd>Novara internal</dd></div>
              <div><dt>Fee</dt><dd><novara-money [minor]="p.fee.amountMinor" currency="INR" /></dd></div>
              <div><dt>Created</dt><dd>{{ p.createdAt | date: 'medium' }}</dd></div>
            </dl>
          </section>
          <aside class="detail-card">
            <span class="eyebrow">STATUS TIMELINE</span>
            <ol class="timeline">
              @for (event of p.timeline; track event.occurredAt) {
                <li><b>{{ event.status }}</b><small>{{ event.occurredAt | date: 'medium' }}</small></li>
              }
            </ol>
          </aside>
        </div>
      }
    }
  `,
  styleUrl: './payments-premium.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentDetailPage {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(PAYMENTS_API);
  protected readonly vm$ = this.route.paramMap.pipe(
    switchMap((parameters) =>
      this.api.getPayment(parameters.get('paymentId')!).pipe(
        map((payment) => ({ loading: false, error: false, payment })),
        startWith({ loading: true, error: false, payment: null }),
        catchError(() => of({ loading: false, error: true, payment: null })),
      ),
    ),
  );
}
