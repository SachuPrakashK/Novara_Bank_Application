import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { ErrorNotificationService } from '../../core/http/error-notification.service';
import { ToastHostComponent } from './toast-host.component';

describe('ToastHostComponent', () => {
  it('renders semantic notifications and dismisses them accessibly', async () => {
    const fixture = TestBed.createComponent(ToastHostComponent);
    const notifications = TestBed.inject(ErrorNotificationService);
    notifications.success('Preferences saved', { title: 'Saved', duration: 0 });
    notifications.error('Network unavailable');
    fixture.detectChanges();

    const toasts = fixture.nativeElement.querySelectorAll('.toast');
    expect(toasts).toHaveLength(2);
    expect(toasts[0].getAttribute('role')).toBe('status');
    expect(toasts[1].getAttribute('role')).toBe('alert');

    const dismiss = fixture.nativeElement.querySelector(
      '[aria-label="Dismiss success notification"]',
    ) as HTMLButtonElement | null;
    dismiss?.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('.toast')).toHaveLength(1);
    notifications.dismiss();
  });
});
