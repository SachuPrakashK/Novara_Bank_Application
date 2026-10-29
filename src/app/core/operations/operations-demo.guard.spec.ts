import { TestBed } from '@angular/core/testing';
import type { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { describe, expect, it } from 'vitest';
import type { NovaraConfig } from '../config/app-config';
import { NOVARA_CONFIG } from '../config/app-config';
import { operationsDemoGuard } from './operations-demo.guard';

describe('operationsDemoGuard', () => {
  function evaluate(config: NovaraConfig): true | ReturnType<Router['createUrlTree']> {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: NOVARA_CONFIG, useValue: config }] });
    return TestBed.runInInjectionContext(() => operationsDemoGuard({} as never, {} as never)) as true | ReturnType<Router['createUrlTree']>;
  }

  const session = { idleTimeoutMs: 900_000, warningDurationMs: 120_000 };

  it('allows only the development mock configuration', () => {
    expect(evaluate({ production: false, useMockApi: true, apiBaseUrl: '/api', session })).toBe(true);
  });

  it('blocks production even if mock mode is accidentally enabled', () => {
    expect(evaluate({ production: true, useMockApi: true, apiBaseUrl: '/api/v1', session }).toString()).toBe('/login');
  });

  it('blocks non-mock development and production configurations', () => {
    expect(evaluate({ production: false, useMockApi: false, apiBaseUrl: '/api/v1', session }).toString()).toBe('/login');
    expect(evaluate({ production: true, useMockApi: false, apiBaseUrl: '/api/v1', session }).toString()).toBe('/login');
  });
});
