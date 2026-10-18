import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { NOVARA_CONFIG } from '../config/app-config';
import type { AuthApi } from './auth-api';
import type { AuthSession, SignInChallenge, SignInCommand, VerifyMfaCommand } from './auth.models';

@Injectable()
export class HttpAuthApi implements AuthApi {
  private readonly http = inject(HttpClient);
  private readonly config = inject(NOVARA_CONFIG);
  private readonly baseUrl = `${this.config.apiBaseUrl}/auth`;

  getSession(): Observable<AuthSession> { return this.http.get<AuthSession>(`${this.baseUrl}/session`); }
  loginUrl(returnTo: string): string { return `${this.baseUrl}/login?returnTo=${encodeURIComponent(returnTo)}`; }
  signIn(command: SignInCommand): Observable<SignInChallenge> { void command; throw new Error('Passwords are accepted only by the identity provider.'); }
  verifyMfa(command: VerifyMfaCommand): Observable<AuthSession> { void command; throw new Error('MFA is performed only by the identity provider.'); }
  continueSession(): Observable<AuthSession> { return this.http.post<AuthSession>(`${this.baseUrl}/session/continue`, {}); }
  signOut(): Observable<void> { return this.http.post<void>(`${this.baseUrl}/logout`, {}); }
}
