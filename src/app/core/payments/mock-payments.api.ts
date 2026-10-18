import { Injectable } from '@angular/core';
import { delay, of, throwError } from 'rxjs';
import type { Observable } from 'rxjs';
import type { PaymentsApi } from './payments-api';
import type { CreateBeneficiaryCommand, CreateScheduleCommand, PaymentBeneficiary, PaymentDraft, PaymentLimits, PaymentPage, PaymentQuote, PaymentResult, PaymentSchedule, SubmitPaymentCommand } from './payment.models';

@Injectable()
export class MockPaymentsApi implements PaymentsApi {
  private beneficiaries: PaymentBeneficiary[] = [
    { id: 'ben-001', name: 'Maya Rao', bankName: 'Novara Bank', maskedAccount: '•••• 3491', favorite: true, status: 'ACTIVE' },
    { id: 'ben-002', name: 'Arjun Mehta', bankName: 'Horizon Bank', maskedAccount: '•••• 7082', favorite: true, status: 'ACTIVE' },
    { id: 'ben-003', name: 'Aster Homes', bankName: 'Unity Bank', maskedAccount: '•••• 1260', favorite: false, status: 'COOLING_PERIOD', availableFrom: '2026-10-16T11:00:00+05:30' },
  ];
  private readonly results = new Map<string, PaymentResult>();
  private schedules: PaymentSchedule[] = [];

  getBeneficiaries(): Observable<readonly PaymentBeneficiary[]> { return of(this.beneficiaries).pipe(delay(250)); }

  createBeneficiary(command: CreateBeneficiaryCommand): Observable<PaymentBeneficiary> {
    const beneficiary: PaymentBeneficiary = { id: crypto.randomUUID(), name: command.nickname?.trim() || command.name.trim(), bankName: 'External bank · verification pending', maskedAccount: `•••• ${command.accountNumber.slice(-4)}`, favorite: false, status: 'COOLING_PERIOD', availableFrom: new Date(Date.now() + 24 * 60 * 60_000).toISOString() };
    this.beneficiaries = [beneficiary, ...this.beneficiaries];
    return of(beneficiary).pipe(delay(500));
  }

  createQuote(draft: PaymentDraft): Observable<PaymentQuote> {
    const beneficiary = this.beneficiaries.find((item) => item.id === draft.beneficiaryId);
    if (!beneficiary || beneficiary.status !== 'ACTIVE') return throwError(() => new Error('Beneficiary is unavailable'));
    const feeMinor = draft.method === 'RTGS' ? 2500 : 0;
    return of<PaymentQuote>({ quoteId: crypto.randomUUID(), sourceAccountId: draft.sourceAccountId, sourceLabel: 'Everyday Savings', sourceMaskedNumber: '•••• 4821', beneficiaryId: beneficiary.id, beneficiaryName: beneficiary.name, beneficiaryMaskedNumber: beneficiary.maskedAccount, amount: draft.amount, fee: { amountMinor: feeMinor, currency: 'INR' }, totalDebit: { amountMinor: draft.amount.amountMinor + feeMinor, currency: 'INR' }, method: draft.method, processingMessage: draft.method === 'NEFT' ? 'Usually credited within 2 business hours' : 'Usually credited within minutes', expiresAt: new Date(Date.now() + 5 * 60_000).toISOString(), note: draft.note }).pipe(delay(450));
  }

  submitPayment(command: SubmitPaymentCommand): Observable<PaymentResult> {
    const existing = this.results.get(command.idempotencyKey);
    if (existing) return of(existing).pipe(delay(150));
    const result: PaymentResult = { outcome: 'COMPLETED', clientRequestId: command.idempotencyKey, paymentId: crypto.randomUUID(), reference: `NVR${Date.now().toString().slice(-10)}`, submittedAt: new Date().toISOString(), message: 'Your transfer instruction was accepted.' };
    this.results.set(command.idempotencyKey, result);
    return of(result).pipe(delay(900));
  }

  getPaymentStatus(paymentId: string): Observable<PaymentResult> {
    const result = [...this.results.values()].find((item) => item.paymentId === paymentId);
    return result ? of(result).pipe(delay(250)) : throwError(() => new Error('Payment not found'));
  }
  getPaymentStatusByIdempotencyKey(key: string): Observable<PaymentResult> {
    const result = this.results.get(key);
    return result ? of(result).pipe(delay(250)) : throwError(() => new Error('Payment not found'));
  }
  getPayments(): Observable<PaymentPage> { return of({ items: [], page: 0, size: 20, total: 0 }).pipe(delay(200)); }
  getPayment(): Observable<PaymentQuote> { return throwError(() => new Error('Payment not found')); }
  getLimits(): Observable<PaymentLimits> { return of<PaymentLimits>({ perPaymentMinor: 50000000, dailyLimitMinor: 100000000, usedTodayMinor: 0, availableTodayMinor: 100000000, currency: 'INR', bankingTimezone: 'Asia/Kolkata' }).pipe(delay(150)); }
  lowerDailyLimit(amountMinor: number): Observable<PaymentLimits> { return of<PaymentLimits>({ perPaymentMinor: Math.min(50000000, amountMinor), dailyLimitMinor: amountMinor, usedTodayMinor: 0, availableTodayMinor: amountMinor, customerSelectedDailyMinor: amountMinor, currency: 'INR', bankingTimezone: 'Asia/Kolkata' }).pipe(delay(200)); }
  getSchedules(): Observable<readonly PaymentSchedule[]> { return of(this.schedules).pipe(delay(150)); }
  createSchedule(command: CreateScheduleCommand): Observable<PaymentSchedule> { const schedule: PaymentSchedule = { id: crypto.randomUUID(), status: 'ACTIVE', recurrence: command.recurrence, timezone: command.timezone, sourceAccountId: command.sourceAccountId, sourceMaskedNumber: '•••• 4821', destinationName: 'Saved recipient', destinationMaskedNumber: '•••• 3491', amountMinor: command.amountMinor, currency: 'INR', nextExecutionAt: command.firstExecutionAt, endAt: command.endAt, maximumOccurrences: command.maximumOccurrences, executedOccurrences: 0, description: command.description }; this.schedules=[schedule,...this.schedules]; return of(schedule).pipe(delay(250)); }
  cancelSchedule(id: string): Observable<PaymentSchedule> { const current=this.schedules.find(s=>s.id===id); if(!current)return throwError(()=>new Error('Schedule not found')); const changed={...current,status:'CANCELLED' as const};this.schedules=this.schedules.map(s=>s.id===id?changed:s);return of(changed).pipe(delay(200)); }
}
