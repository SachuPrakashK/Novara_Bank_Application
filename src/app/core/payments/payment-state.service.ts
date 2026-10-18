import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { NovaraApiError } from '../http/api-error';
import type { PaymentDraft, PaymentQuote, PaymentResult } from './payment.models';
import { PAYMENTS_API } from './payments-api';

@Injectable({ providedIn: 'root' })
export class PaymentStateService {
  private readonly api = inject(PAYMENTS_API);
  private readonly quoteState = signal<PaymentQuote | null>(null);
  private readonly resultState = signal<PaymentResult | null>(null);
  private idempotencyKey?: string;
  private submission?: Promise<PaymentResult>;

  readonly quote = this.quoteState.asReadonly();
  readonly result = this.resultState.asReadonly();
  readonly submitting = computed(() => Boolean(this.submission));

  async prepare(draft: PaymentDraft): Promise<PaymentQuote> {
    const quote = await firstValueFrom(this.api.createQuote(draft));
    this.quoteState.set(quote);
    this.resultState.set(null);
    this.idempotencyKey = crypto.randomUUID();
    return quote;
  }

  submit(): Promise<PaymentResult> {
    if (this.submission) return this.submission;
    const quote = this.quoteState();
    if (!quote || !this.idempotencyKey) return Promise.reject(new Error('Payment review is required before submission.'));
    const clientRequestId = this.idempotencyKey;
    this.submission = firstValueFrom(this.api.submitPayment({ quoteId: quote.quoteId, idempotencyKey: clientRequestId, quote }))
      .catch((error: unknown): PaymentResult => {
        if (error instanceof NovaraApiError && (error.kind === 'NETWORK' || error.kind === 'TIMEOUT' || error.kind === 'SERVER')) {
          return { outcome: 'UNKNOWN', clientRequestId, submittedAt: new Date().toISOString(), message: 'We could not confirm the final status. Do not send this payment again. Check its status instead.' };
        }
        throw error;
      })
      .then((result) => { this.resultState.set(result); return result; })
      .finally(() => { this.submission = undefined; });
    return this.submission;
  }

  async refreshStatus(): Promise<PaymentResult> {
    const current = this.resultState();
    if (!current) throw new Error('There is no transfer to check.');
    const request = current.paymentId ? this.api.getPaymentStatus(current.paymentId) : this.api.getPaymentStatusByIdempotencyKey(current.clientRequestId);
    const result = await firstValueFrom(request);
    this.resultState.set(result);
    return result;
  }

  clear(): void { this.quoteState.set(null); this.resultState.set(null); this.idempotencyKey = undefined; this.submission = undefined; }
}
