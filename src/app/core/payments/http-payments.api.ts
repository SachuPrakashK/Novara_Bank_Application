import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, of, type Observable } from 'rxjs';
import { NOVARA_CONFIG } from '../config/app-config';
import type { PaymentsApi } from './payments-api';
import type { CreateBeneficiaryCommand, PaymentBeneficiary, PaymentDraft, PaymentQuote, PaymentResult, SubmitPaymentCommand } from './payment.models';

@Injectable()
export class HttpPaymentsApi implements PaymentsApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(NOVARA_CONFIG).apiBaseUrl;
  getBeneficiaries(): Observable<readonly PaymentBeneficiary[]> { return this.http.get<readonly PaymentBeneficiary[]>(`${this.baseUrl}/beneficiaries`); }
  createBeneficiary(command: CreateBeneficiaryCommand): Observable<PaymentBeneficiary> { return this.http.post<PaymentBeneficiary>(`${this.baseUrl}/beneficiaries`, command); }
  createQuote(draft: PaymentDraft): Observable<PaymentQuote> {
    const quote: PaymentQuote = {
      quoteId: crypto.randomUUID(),
      sourceAccountId: draft.sourceAccountId,
      sourceLabel: 'Novara account',
      sourceMaskedNumber: '',
      beneficiaryId: draft.beneficiaryId,
      beneficiaryName: 'Destination account',
      beneficiaryMaskedNumber: '',
      amount: draft.amount,
      fee: { amountMinor: 0, currency: draft.amount.currency },
      totalDebit: draft.amount,
      method: 'INTERNAL',
      processingMessage: 'Internal transfers are posted immediately after server validation.',
      expiresAt: new Date(Date.now() + 5 * 60_000).toISOString(),
      note: draft.note,
    };
    return of(quote);
  }
  submitPayment(command: SubmitPaymentCommand): Observable<PaymentResult> {
    if (!command.quote) throw new Error('The reviewed transfer details are required.');
    const quote = command.quote;
    return this.http.post<TransferResponse>(`${this.baseUrl}/transfers/internal`, {
      sourceAccountId: quote.sourceAccountId,
      destinationAccountId: quote.beneficiaryId,
      amountMinor: quote.amount.amountMinor,
      currency: quote.amount.currency,
      note: quote.note,
    }, { headers: { 'Idempotency-Key': command.idempotencyKey } }).pipe(map((transfer) => toPaymentResult(transfer, command.idempotencyKey)));
  }
  getPaymentStatus(paymentId: string): Observable<PaymentResult> {
    return this.http.get<TransferResponse>(`${this.baseUrl}/transfers/${encodeURIComponent(paymentId)}`).pipe(map((transfer) => toPaymentResult(transfer, paymentId)));
  }
  getPaymentStatusByIdempotencyKey(key: string): Observable<PaymentResult> {
    return this.http.get<TransferResponse>(`${this.baseUrl}/transfers/by-idempotency-key/${encodeURIComponent(key)}`).pipe(map((transfer) => toPaymentResult(transfer, key)));
  }
}

interface TransferResponse { readonly id: string; readonly status: 'PROCESSING' | 'COMPLETED' | 'FAILED'; readonly reference: string; readonly createdAt: string; }

function toPaymentResult(transfer: TransferResponse, clientRequestId: string): PaymentResult {
  return {
    outcome: transfer.status,
    clientRequestId,
    paymentId: transfer.id,
    reference: transfer.reference,
    submittedAt: transfer.createdAt,
    message: transfer.status === 'COMPLETED' ? 'Transfer completed.' : 'Transfer is being processed.',
  };
}
