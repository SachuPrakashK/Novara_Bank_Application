import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { NOVARA_CONFIG } from '../config/app-config';
import { SessionService } from '../session/session.service';
import { NovaraApiError } from './api-error';
import { apiErrorInterceptor, bankingContextInterceptor } from './http.interceptors';

describe('banking HTTP interceptors', () => {
  function setup(): { client: HttpClient; http: HttpTestingController } {
    TestBed.configureTestingModule({ providers: [provideHttpClient(withInterceptors([bankingContextInterceptor, apiErrorInterceptor])), provideHttpClientTesting(), { provide: NOVARA_CONFIG, useValue: { apiBaseUrl: '/api', production: false, useMockApi: false, session: { idleTimeoutMs: 1000, warningDurationMs: 200 } } }] });
    return { client: TestBed.inject(HttpClient), http: TestBed.inject(HttpTestingController) };
  }

  it('adds credentials and a unique request ID only to banking API calls', () => {
    const { client, http } = setup();
    client.get('/api/accounts').subscribe();
    const request = http.expectOne('/api/accounts');
    expect(request.request.withCredentials).toBe(true);
    expect(request.request.headers.get('X-Request-ID')).toMatch(/^[0-9a-f-]{36}$/);
    request.flush([]);
    http.verify();
  });

  it('normalizes server errors without exposing backend response content', () => {
    const { client, http } = setup();
    let received: unknown;
    client.get('/api/accounts').subscribe({ error: (error: unknown) => { received = error; } });
    http.expectOne('/api/accounts').flush({ internalMessage: 'database host secret' }, { status: 503, statusText: 'Unavailable', headers: { 'X-Correlation-ID': 'corr-safe-123' } });
    expect(received).toBeInstanceOf(NovaraApiError);
    expect((received as NovaraApiError).message).not.toContain('database');
    expect((received as NovaraApiError).correlationId).toBe('corr-safe-123');
  });

  it('expires the local session when an authenticated API call returns 401', () => {
    const { client, http } = setup();
    const session = TestBed.inject(SessionService);
    session.start();
    client.get('/api/accounts').subscribe({ error: () => undefined });
    http.expectOne('/api/accounts').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(session.sessionState()).toBe('expired');
  });
});
