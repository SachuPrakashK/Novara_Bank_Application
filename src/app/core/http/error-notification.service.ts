import { Injectable, signal } from '@angular/core';

export type NotificationTone = 'success' | 'error' | 'warning' | 'info';

export interface SafeNotification {
  readonly id: string;
  readonly tone: NotificationTone;
  readonly message: string;
  readonly title?: string;
  readonly actionLabel?: string;
  readonly action?: () => void;
}

export interface NotificationOptions {
  readonly title?: string;
  readonly duration?: number;
  readonly actionLabel?: string;
  readonly action?: () => void;
}

@Injectable({ providedIn: 'root' })
export class ErrorNotificationService {
  private readonly state = signal<readonly SafeNotification[]>([]);
  private readonly timers = new Map<string, ReturnType<typeof setTimeout>>();
  readonly notifications = this.state.asReadonly();

  /** Backward-compatible safe error entry point used by the HTTP interceptor. */
  show(message: string, actionLabel?: string): void {
    this.error(message, { actionLabel });
  }

  success(message: string, options: NotificationOptions = {}): void {
    this.add('success', message, { duration: 5_000, ...options });
  }

  error(message: string, options: NotificationOptions = {}): void {
    this.add('error', message, { duration: 0, ...options });
  }

  warning(message: string, options: NotificationOptions = {}): void {
    this.add('warning', message, { duration: 8_000, ...options });
  }

  info(message: string, options: NotificationOptions = {}): void {
    this.add('info', message, { duration: 6_000, ...options });
  }

  dismiss(id?: string): void {
    if (!id) {
      for (const timer of this.timers.values()) clearTimeout(timer);
      this.timers.clear();
      this.state.set([]);
      return;
    }
    const timer = this.timers.get(id);
    if (timer) clearTimeout(timer);
    this.timers.delete(id);
    this.state.update((items) => items.filter((item) => item.id !== id));
  }

  invokeAction(notification: SafeNotification): void {
    notification.action?.();
    this.dismiss(notification.id);
  }

  private add(tone: NotificationTone, message: string, options: NotificationOptions): void {
    const normalizedMessage = message.trim();
    if (!normalizedMessage) return;
    const duplicate = this.state().find(
      (item) => item.tone === tone && item.message === normalizedMessage && item.title === options.title,
    );
    if (duplicate) return;

    const notification: SafeNotification = {
      id: crypto.randomUUID(),
      tone,
      message: normalizedMessage,
      title: options.title,
      actionLabel: options.actionLabel,
      action: options.action,
    };
    this.state.update((items) => [...items.slice(-3), notification]);
    if ((options.duration ?? 0) > 0) {
      this.timers.set(
        notification.id,
        setTimeout(() => this.dismiss(notification.id), options.duration),
      );
    }
  }
}
