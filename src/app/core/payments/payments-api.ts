import { InjectionToken } from '@angular/core';
import type { Observable } from 'rxjs';
import type { CreateBeneficiaryCommand, PaymentBeneficiary, PaymentDraft, PaymentQuote, PaymentResult, SubmitPaymentCommand } from './payment.models';

export interface PaymentsApi {
  getBeneficiaries(): Observable<readonly PaymentBeneficiary[]>;
  createBeneficiary(command: CreateBeneficiaryCommand): Observable<PaymentBeneficiary>;
  createQuote(draft: PaymentDraft): Observable<PaymentQuote>;
  submitPayment(command: SubmitPaymentCommand): Observable<PaymentResult>;
  getPaymentStatus(paymentId: string): Observable<PaymentResult>;
  getPaymentStatusByIdempotencyKey(key: string): Observable<PaymentResult>;
}

export const PAYMENTS_API = new InjectionToken<PaymentsApi>('PAYMENTS_API');
