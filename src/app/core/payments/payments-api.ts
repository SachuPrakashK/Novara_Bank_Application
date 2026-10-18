import { InjectionToken } from '@angular/core';
import type { Observable } from 'rxjs';
import type { CreateBeneficiaryCommand, CreateScheduleCommand, PaymentBeneficiary, PaymentDraft, PaymentLimits, PaymentPage, PaymentQuote, PaymentResult, PaymentSchedule, SubmitPaymentCommand } from './payment.models';

export interface PaymentsApi {
  getBeneficiaries(): Observable<readonly PaymentBeneficiary[]>;
  createBeneficiary(command: CreateBeneficiaryCommand): Observable<PaymentBeneficiary>;
  createQuote(draft: PaymentDraft): Observable<PaymentQuote>;
  submitPayment(command: SubmitPaymentCommand): Observable<PaymentResult>;
  getPaymentStatus(paymentId: string): Observable<PaymentResult>;
  getPaymentStatusByIdempotencyKey(key: string): Observable<PaymentResult>;
  getPayments(query?: string, status?: string, page?: number): Observable<PaymentPage>;
  getPayment(paymentId: string): Observable<PaymentQuote>;
  getLimits(): Observable<PaymentLimits>;
  lowerDailyLimit(amountMinor: number): Observable<PaymentLimits>;
  getSchedules(): Observable<readonly PaymentSchedule[]>;
  createSchedule(command: CreateScheduleCommand): Observable<PaymentSchedule>;
  cancelSchedule(id: string): Observable<PaymentSchedule>;
}

export const PAYMENTS_API = new InjectionToken<PaymentsApi>('PAYMENTS_API');
