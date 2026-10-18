import { TestBed } from '@angular/core/testing';
import type { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { describe, expect, it } from 'vitest';
import { NOVARA_CONFIG } from '../config/app-config';
import { AUTH_API } from './auth-api';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';
import { MockAuthApi } from './mock-auth.api';

const config = { production: false, useMockApi: true, apiBaseUrl: '/api', session: { idleTimeoutMs: 900_000, warningDurationMs: 120_000 } } as const;
describe('authGuard', () => {
  function configure(): void { TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: NOVARA_CONFIG, useValue: config }, MockAuthApi, { provide: AUTH_API, useExisting: MockAuthApi }] }); }
  it('redirects anonymous visitors to login', () => { configure(); const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never)); expect((result as ReturnType<Router['createUrlTree']>).toString()).toBe('/login'); });
  it('allows an authenticated session', () => { configure(); TestBed.inject(AuthService).useDemoSession(); expect(TestBed.runInInjectionContext(() => authGuard({} as never, {} as never))).toBe(true); });
});
