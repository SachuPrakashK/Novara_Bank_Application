import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { NOVARA_CONFIG } from '../config/app-config';
import { HttpAuthApi } from './http-auth.api';

describe('HttpAuthApi contract', () => {
  function setup(): { api: HttpAuthApi; http: HttpTestingController } {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), { provide: NOVARA_CONFIG, useValue: { apiBaseUrl: '/api', production: false, useMockApi: false, session: { idleTimeoutMs: 1000, warningDurationMs: 200 } } }, HttpAuthApi] });
    return { api: TestBed.inject(HttpAuthApi), http: TestBed.inject(HttpTestingController) };
  }

  it('redirects to the BFF and never posts credentials', () => {
    const { api, http } = setup();
    expect(api.loginUrl('/dashboard')).toBe('/api/auth/login?returnTo=%2Fdashboard');
    expect(() => api.signIn({ username: 'customer', password: 'secret-value', rememberDevice: false })).toThrow();
    http.expectNone('/api/auth/sign-in');
    http.verify();
  });

  it('uses a dedicated endpoint to continue the server session', () => {
    const { api, http } = setup();
    api.continueSession().subscribe();
    const request = http.expectOne('/api/auth/session/continue');
    expect(request.request.method).toBe('POST');
    request.flush({ authenticated: true, expiresAt: new Date().toISOString() });
    http.verify();
  });
});
