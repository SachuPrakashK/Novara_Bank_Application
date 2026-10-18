import { computed, effect, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { SessionService } from '../session/session.service';
import { AUTH_API } from './auth-api';
import type { AuthSession, AuthStatus, SignInChallenge } from './auth.models';
import { MockAuthApi } from './mock-auth.api';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AUTH_API);
  private readonly mockApi = inject(MockAuthApi);
  private readonly session = inject(SessionService);
  private readonly status = signal<AuthStatus>('initializing');
  private readonly challenge = signal<SignInChallenge | null>(null);
  private readonly customerState = signal<AuthSession['customer']>(undefined);
  readonly authStatus = this.status.asReadonly();
  readonly customer = this.customerState.asReadonly();
  readonly pendingChallenge = this.challenge.asReadonly();
  readonly isAuthenticated = computed(() => this.status() === 'authenticated');

  constructor() {
    effect(() => { if (this.session.sessionState() === 'expired') this.status.set('expired'); });
  }

  async initialize(): Promise<void> {
    try { this.applySession(await firstValueFrom(this.api.getSession())); }
    catch { this.clearLocalSession('anonymous'); }
  }

  async beginSignIn(username: string, password: string, rememberDevice = false): Promise<void> {
    const challenge = await firstValueFrom(this.api.signIn({ username, password, rememberDevice }));
    this.challenge.set(challenge);
    this.status.set('mfa-required');
  }

  redirectToIdentityProvider(returnTo = '/dashboard'): void { window.location.assign(this.api.loginUrl(returnTo)); }

  async verifyMfa(code: string): Promise<boolean> {
    const challenge = this.challenge();
    if (!challenge) return false;
    const session = await firstValueFrom(this.api.verifyMfa({ challengeId: challenge.challengeId, code }));
    this.applySession(session);
    return session.authenticated;
  }

  async continueSession(): Promise<boolean> {
    try { const session = await firstValueFrom(this.api.continueSession()); this.applySession(session); return session.authenticated; }
    catch { this.session.expire('server'); return false; }
  }

  useDemoSession(): void { this.applySession(this.mockApi.createDemoSession()); }

  async signOut(): Promise<void> {
    try { await firstValueFrom(this.api.signOut()); }
    finally { this.clearLocalSession('anonymous'); this.session.expire('signed-out'); }
  }

  acknowledgeExpiry(): void { this.clearLocalSession('anonymous'); }

  private applySession(session: AuthSession): void {
    if (!session.authenticated) { this.clearLocalSession('anonymous'); return; }
    this.customerState.set(session.customer);
    this.challenge.set(null);
    this.status.set('authenticated');
    this.session.start(session.expiresAt);
  }

  private clearLocalSession(status: AuthStatus): void { this.customerState.set(undefined); this.challenge.set(null); this.status.set(status); }
}
