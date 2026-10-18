import { DOCUMENT } from '@angular/common';
import { computed, inject, Injectable, signal } from '@angular/core';
import { auditTime, fromEvent, merge } from 'rxjs';
import type { Subscription } from 'rxjs';
import { NOVARA_CONFIG } from '../config/app-config';

export type SessionState = 'inactive' | 'active' | 'warning' | 'expired';
export type ExpiryReason = 'idle' | 'server' | 'signed-out';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly document = inject(DOCUMENT);
  private readonly config = inject(NOVARA_CONFIG);
  private readonly state = signal<SessionState>('inactive');
  private readonly deadline = signal<number | null>(null);
  private readonly clock = signal(Date.now());
  private serverDeadline?: number;
  private warningTimer?: ReturnType<typeof setTimeout>;
  private expiryTimer?: ReturnType<typeof setTimeout>;
  private clockTimer?: ReturnType<typeof setInterval>;
  private activitySubscription?: Subscription;

  readonly sessionState = this.state.asReadonly();
  readonly remainingSeconds = computed(() => Math.max(0, Math.ceil(((this.deadline() ?? this.clock()) - this.clock()) / 1000)));

  start(serverExpiresAt?: string): void {
    this.clearTimers();
    const parsedDeadline = serverExpiresAt ? Date.parse(serverExpiresAt) : Number.NaN;
    this.serverDeadline = Number.isFinite(parsedDeadline) ? parsedDeadline : undefined;
    this.state.set('active');
    this.bindActivity();
    this.schedule(serverExpiresAt);
  }

  recordActivity(): void {
    if (this.state() !== 'active') return;
    this.schedule();
  }

  continue(serverExpiresAt?: string): void { this.start(serverExpiresAt); }

  expire(reason: ExpiryReason): void {
    this.clearTimers();
    this.activitySubscription?.unsubscribe();
    this.activitySubscription = undefined;
    this.deadline.set(null);
    this.serverDeadline = undefined;
    this.state.set(reason === 'signed-out' ? 'inactive' : 'expired');
  }

  private schedule(serverExpiresAt?: string): void {
    this.clearTimers();
    const policyDeadline = Date.now() + this.config.session.idleTimeoutMs;
    const serverDeadline = serverExpiresAt ? Date.parse(serverExpiresAt) : this.serverDeadline;
    const expiryAt = Math.min(policyDeadline, serverDeadline && Number.isFinite(serverDeadline) ? serverDeadline : policyDeadline);
    const warningAt = Math.max(Date.now(), expiryAt - this.config.session.warningDurationMs);
    this.deadline.set(expiryAt);
    this.warningTimer = setTimeout(() => this.showWarning(), Math.max(0, warningAt - Date.now()));
    this.expiryTimer = setTimeout(() => this.expire('idle'), Math.max(0, expiryAt - Date.now()));
  }

  private showWarning(): void {
    if (this.state() !== 'active') return;
    this.state.set('warning');
    this.clock.set(Date.now());
    this.clockTimer = setInterval(() => this.clock.set(Date.now()), 1000);
  }

  private bindActivity(): void {
    if (this.activitySubscription) return;
    this.activitySubscription = merge(
      fromEvent(this.document, 'pointerdown'),
      fromEvent(this.document, 'keydown'),
      fromEvent(this.document, 'scroll'),
    ).pipe(auditTime(1000)).subscribe(() => this.recordActivity());
  }

  private clearTimers(): void {
    if (this.warningTimer) clearTimeout(this.warningTimer);
    if (this.expiryTimer) clearTimeout(this.expiryTimer);
    if (this.clockTimer) clearInterval(this.clockTimer);
    this.warningTimer = undefined;
    this.expiryTimer = undefined;
    this.clockTimer = undefined;
  }
}
