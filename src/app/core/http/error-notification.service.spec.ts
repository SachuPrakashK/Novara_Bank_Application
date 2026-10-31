import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ErrorNotificationService } from './error-notification.service';

describe('ErrorNotificationService', () => {
  let service: ErrorNotificationService;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({});
    service = TestBed.inject(ErrorNotificationService);
  });

  afterEach(() => {
    service.dismiss();
    vi.useRealTimers();
  });

  it('supports all notification tones and caps the visible stack', () => {
    service.success('Saved');
    service.info('Information');
    service.warning('Check this');
    service.error('Could not save');
    service.info('Newest');

    expect(service.notifications().map((item) => item.tone)).toEqual([
      'info',
      'warning',
      'error',
      'info',
    ]);
  });

  it('prevents duplicate messages and permits manual dismissal', () => {
    service.error('Safe message');
    service.error('Safe message');
    expect(service.notifications()).toHaveLength(1);

    service.dismiss(service.notifications()[0].id);
    expect(service.notifications()).toHaveLength(0);
  });

  it('automatically dismisses transient notifications but keeps errors persistent', () => {
    service.success('Saved');
    service.error('Persistent');
    vi.advanceTimersByTime(5_000);

    expect(service.notifications().map((item) => item.message)).toEqual(['Persistent']);
  });
});
