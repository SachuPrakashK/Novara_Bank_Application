import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NOVARA_CONFIG } from '../../core/config/app-config';

interface BrowserSession { readonly id: string; readonly createdAt: string; readonly lastAccessedAt: string; readonly expiresAt: string; readonly current: boolean; }
interface SecurityEvent { readonly action: string; readonly outcome: string; readonly occurredAt: string; }

@Component({
  selector: 'app-security-center',
  template: `<section class="feature-page"><p class="overline">IDENTITY & ACCESS</p><h1>Security center</h1><p>Your identity provider controls passwords, MFA, passkeys, and recovery. Novara controls these browser sessions.</p>
    <h2>Active sessions</h2>
    @if (loading()) { <p>Loading secure session information…</p> }
    @for (session of sessions(); track session.id) { <article><strong>{{ session.current ? 'This session' : 'Browser session' }}</strong><p>Last active {{ session.lastAccessedAt | date:'medium' }} · Expires {{ session.expiresAt | date:'medium' }}</p>@if (!session.current) { <button type="button" (click)="terminate(session.id)">Terminate session</button> }</article> }
    <h2>Recent security activity</h2>
    @for (event of events(); track event.occurredAt) { <p>{{ event.action }} · {{ event.outcome }} · {{ event.occurredAt | date:'medium' }}</p> } @empty { <p>No recent session-security events.</p> }
  </section>`,
  styleUrl: '../feature-page.scss',
  imports: [DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SecurityCenterPage {
  private readonly http = inject(HttpClient); private readonly base = `${inject(NOVARA_CONFIG).apiBaseUrl}/security`;
  protected readonly sessions = signal<BrowserSession[]>([]); protected readonly events = signal<SecurityEvent[]>([]); protected readonly loading = signal(true);
  constructor() { this.refresh(); }
  protected terminate(id: string): void { this.http.delete<void>(`${this.base}/sessions/${encodeURIComponent(id)}`).subscribe(() => this.refresh()); }
  private refresh(): void {
    this.loading.set(true);
    this.http.get<BrowserSession[]>(`${this.base}/sessions`).subscribe({ next: value => { this.sessions.set(value); this.loading.set(false); }, error: () => this.loading.set(false) });
    this.http.get<SecurityEvent[]>(`${this.base}/events`).subscribe(value => this.events.set(value));
  }
}
