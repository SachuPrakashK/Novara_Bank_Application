import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { NOVARA_CONFIG } from '../config/app-config';
import { AUTH_API } from './auth-api';
import { AuthService } from './auth.service';
import { MockAuthApi } from './mock-auth.api';
describe('AuthService', () => {
  it('requires MFA before creating an authenticated session', async () => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({ providers: [{ provide: NOVARA_CONFIG, useValue: { production: false, useMockApi: true, apiBaseUrl: '/api', session: { idleTimeoutMs: 900_000, warningDurationMs: 120_000 } } }, MockAuthApi, { provide: AUTH_API, useExisting: MockAuthApi }] });
    const service = TestBed.inject(AuthService);
    const pending = service.beginSignIn('fictional-user', 'not-retained');
    await vi.runAllTimersAsync(); await pending;
    expect(service.authStatus()).toBe('mfa-required');
    expect(service.isAuthenticated()).toBe(false);
    vi.useRealTimers();
  });
});
