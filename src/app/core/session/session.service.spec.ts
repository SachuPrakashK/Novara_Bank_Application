import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { NOVARA_CONFIG } from '../config/app-config';
import { SessionService } from './session.service';

describe('SessionService', () => {
  it('warns before expiry and ends an idle session', () => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({ providers: [{ provide: NOVARA_CONFIG, useValue: { apiBaseUrl: '/api', production: false, useMockApi: true, session: { idleTimeoutMs: 1000, warningDurationMs: 200 } } }] });
    const session = TestBed.inject(SessionService);
    session.start();
    vi.advanceTimersByTime(800);
    expect(session.sessionState()).toBe('warning');
    vi.advanceTimersByTime(200);
    expect(session.sessionState()).toBe('expired');
    vi.useRealTimers();
  });
});
