import { Injectable, signal } from '@angular/core';

export interface SafeNotification { readonly id: string; readonly message: string; readonly actionLabel?: string; }

@Injectable({ providedIn: 'root' })
export class ErrorNotificationService {
  private readonly currentState = signal<SafeNotification | null>(null);
  readonly current = this.currentState.asReadonly();
  show(message: string, actionLabel?: string): void { this.currentState.set({ id: crypto.randomUUID(), message, actionLabel }); }
  dismiss(): void { this.currentState.set(null); }
}
