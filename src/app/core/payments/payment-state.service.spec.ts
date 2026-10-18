import { TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import type { Observable } from 'rxjs';
import { describe, expect, it } from 'vitest';
import { NovaraApiError } from '../http/api-error';
import type { PaymentDraft, PaymentQuote, PaymentResult, SubmitPaymentCommand } from './payment.models';
import type { PaymentsApi } from './payments-api';
import { PAYMENTS_API } from './payments-api';
import { PaymentStateService } from './payment-state.service';

const draft: PaymentDraft = { sourceAccountId: 'acc-1', beneficiaryId: 'ben-1', amount: { amountMinor: 10000, currency: 'INR' }, method: 'IMPS' };
const quote: PaymentQuote = { quoteId: 'quote-1', sourceAccountId: 'acc-1', sourceLabel: 'Savings', sourceMaskedNumber: '•••• 0001', beneficiaryId: 'ben-1', beneficiaryName: 'Maya', beneficiaryMaskedNumber: '•••• 0002', amount: draft.amount, fee: { amountMinor: 0, currency: 'INR' }, totalDebit: draft.amount, method: 'IMPS', processingMessage: 'Within minutes', expiresAt: new Date(Date.now() + 60_000).toISOString() };

class PaymentsApiStub implements PaymentsApi {
  readonly submission = new Subject<PaymentResult>();
  submitCalls = 0;
  lastCommand?: SubmitPaymentCommand;
  getBeneficiaries = () => of([]);
  createBeneficiary = () => { throw new Error('Not used'); };
  createQuote = () => of(quote);
  submitPayment(command: SubmitPaymentCommand): Observable<PaymentResult> { this.submitCalls += 1; this.lastCommand = command; return this.submission; }
  getPaymentStatus = () => { throw new Error('Not used'); };
  getPaymentStatusByIdempotencyKey = () => { throw new Error('Not used'); };
  getPayments = () => of({items:[],page:0,size:20,total:0}); getPayment = () => of(quote); getLimits = () => { throw new Error('Not used'); }; lowerDailyLimit = () => { throw new Error('Not used'); }; getSchedules = () => of([]); createSchedule = () => { throw new Error('Not used'); }; cancelSchedule = () => { throw new Error('Not used'); };
}

describe('PaymentStateService', () => {
  it('coalesces repeated submit calls behind one idempotent request', async () => {
    const api = new PaymentsApiStub();
    TestBed.configureTestingModule({ providers: [{ provide: PAYMENTS_API, useValue: api }] });
    const state = TestBed.inject(PaymentStateService);
    await state.prepare(draft);
    const first = state.submit(); const second = state.submit();
    expect(first).toBe(second); expect(api.submitCalls).toBe(1); expect(api.lastCommand?.idempotencyKey).toMatch(/^[0-9a-f-]{36}$/);
    api.submission.next({ outcome: 'COMPLETED', clientRequestId: api.lastCommand?.idempotencyKey ?? '', submittedAt: new Date().toISOString(), message: 'Accepted' }); api.submission.complete();
    await first;
  });

  it('maps an uncertain network response to UNKNOWN instead of FAILED', async () => {
    const api: PaymentsApi = { getBeneficiaries: () => of([]), createBeneficiary: () => { throw new Error('Not used'); }, createQuote: () => of(quote), submitPayment: () => throwError(() => new NovaraApiError('NETWORK', 'Offline', 0, undefined, true)), getPaymentStatus: () => { throw new Error('Not used'); }, getPaymentStatusByIdempotencyKey: () => of({ outcome: 'COMPLETED', clientRequestId: 'request', submittedAt: new Date().toISOString(), message: 'Completed' }),getPayments:()=>of({items:[],page:0,size:20,total:0}),getPayment:()=>of(quote),getLimits:()=>{throw new Error('Not used')},lowerDailyLimit:()=>{throw new Error('Not used')},getSchedules:()=>of([]),createSchedule:()=>{throw new Error('Not used')},cancelSchedule:()=>{throw new Error('Not used')} };
    TestBed.configureTestingModule({ providers: [{ provide: PAYMENTS_API, useValue: api }] });
    const state = TestBed.inject(PaymentStateService);
    await state.prepare(draft);
    expect((await state.submit()).outcome).toBe('UNKNOWN');
    expect((await state.refreshStatus()).outcome).toBe('COMPLETED');
  });
});
