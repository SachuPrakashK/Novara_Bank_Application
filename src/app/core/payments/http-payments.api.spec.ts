import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { NOVARA_CONFIG } from '../config/app-config';
import { HttpPaymentsApi } from './http-payments.api';

describe('HttpPaymentsApi contract', () => {
  it('maps a reviewed payment to the internal-transfer contract and sends its idempotency key', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), { provide: NOVARA_CONFIG, useValue: { apiBaseUrl: '/api', production: false, useMockApi: false, session: { idleTimeoutMs: 1000, warningDurationMs: 200 } } }, HttpPaymentsApi] });
    const api = TestBed.inject(HttpPaymentsApi); const http = TestBed.inject(HttpTestingController);
    const quote = { quoteId: 'quote-1', sourceAccountId: 'source-1', sourceLabel: 'Source', sourceMaskedNumber: '1234', beneficiaryId: 'destination-1', beneficiaryName: 'Destination', beneficiaryMaskedNumber: '5678', amount: { amountMinor: 1250, currency: 'INR' as const }, fee: { amountMinor: 0, currency: 'INR' as const }, totalDebit: { amountMinor: 1250, currency: 'INR' as const }, method: 'INTERNAL' as const, processingMessage: 'Immediate', expiresAt: new Date().toISOString(), note: 'Rent' };
    api.submitPayment({ quoteId: 'quote-1', idempotencyKey: 'idempotency-1', quote }).subscribe();
    const request = http.expectOne('/api/transfers/internal');
    expect(request.request.method).toBe('POST'); expect(request.request.headers.get('Idempotency-Key')).toBe('idempotency-1');
    expect(request.request.body).toEqual({ sourceAccountId: 'source-1', destinationAccountId: 'destination-1', amountMinor: 1250, currency: 'INR', note: 'Rent' });
    request.flush({ id: 'transfer-1', status: 'COMPLETED', reference: 'NOV-1', createdAt: new Date().toISOString() }); http.verify();
  });
});
