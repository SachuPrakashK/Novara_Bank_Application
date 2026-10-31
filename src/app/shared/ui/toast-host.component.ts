import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ErrorNotificationService } from '../../core/http/error-notification.service';
import type { NotificationTone } from '../../core/http/error-notification.service';
import { IconComponent, type NovaraIcon } from './icon.component';

@Component({
  selector: 'novara-toast-host',
  imports: [IconComponent],
  template: `
    <section class="toast-region" aria-label="Notifications">
      @for (notification of notifications.notifications(); track notification.id) {
        <article
          class="toast"
          [class]="'toast toast--' + notification.tone"
          [attr.role]="notification.tone === 'error' ? 'alert' : 'status'"
          [attr.aria-live]="notification.tone === 'error' ? 'assertive' : 'polite'"
        >
          <span class="toast__icon" aria-hidden="true">
            <novara-icon [name]="icon(notification.tone)" />
          </span>
          <div class="toast__content">
            @if (notification.title) { <strong>{{ notification.title }}</strong> }
            <p>{{ notification.message }}</p>
            @if (notification.actionLabel && notification.action) {
              <button class="toast__action" type="button" (click)="notifications.invokeAction(notification)">
                {{ notification.actionLabel }}
              </button>
            }
          </div>
          <button
            class="toast__close"
            type="button"
            (click)="notifications.dismiss(notification.id)"
            [attr.aria-label]="'Dismiss ' + notification.tone + ' notification'"
          ><novara-icon name="close" /></button>
        </article>
      }
    </section>
  `,
  styles: [`
    :host { position: fixed; z-index: 100; inset: 1rem 1rem auto auto; pointer-events: none; }
    .toast-region { display: grid; gap: .75rem; width: min(24rem, calc(100vw - 2rem)); }
    .toast { --toast-accent: var(--novara-info); display: grid; grid-template-columns: auto 1fr auto; gap: .75rem; align-items: start; padding: .9rem; border: 1px solid color-mix(in srgb, var(--toast-accent) 45%, var(--novara-border)); border-left: 3px solid var(--toast-accent); border-radius: .85rem; background: var(--novara-surface-elevated); color: var(--novara-text-primary); box-shadow: var(--novara-shadow-lg); pointer-events: auto; animation: toast-in var(--novara-motion-standard) ease-out; }
    .toast--success { --toast-accent: var(--novara-success); }
    .toast--error { --toast-accent: var(--novara-danger); }
    .toast--warning { --toast-accent: var(--novara-warning); }
    .toast__icon { display: grid; place-items: center; width: 2rem; height: 2rem; border-radius: .55rem; background: color-mix(in srgb, var(--toast-accent) 16%, transparent); color: var(--toast-accent); }
    .toast__icon novara-icon { width: 1.05rem; height: 1.05rem; }
    .toast__content { min-width: 0; }
    .toast__content strong { display: block; margin-bottom: .15rem; font-size: .78rem; }
    .toast__content p { margin: 0; color: var(--novara-text-secondary); font-size: .75rem; line-height: 1.5; overflow-wrap: anywhere; }
    .toast__action { margin-top: .45rem; padding: 0; border: 0; background: transparent; color: var(--toast-accent); font-size: .72rem; font-weight: 700; cursor: pointer; }
    .toast__close { display: grid; place-items: center; width: 2rem; height: 2rem; padding: 0; border: 0; border-radius: .5rem; background: transparent; color: var(--novara-text-secondary); cursor: pointer; }
    .toast__close:hover { background: var(--novara-elevated-hover); color: var(--novara-text-primary); }
    @keyframes toast-in { from { opacity: 0; transform: translateY(-.5rem); } }
    @media (max-width: 560px) { :host { inset: .75rem .75rem auto; } .toast-region { width: 100%; } }
    @media (prefers-reduced-motion: reduce) { .toast { animation: none; } }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToastHostComponent {
  protected readonly notifications = inject(ErrorNotificationService);
  protected icon(tone: NotificationTone): NovaraIcon {
    return tone === 'success' ? 'check' : tone === 'error' || tone === 'warning' ? 'alert' : 'bell';
  }
}
