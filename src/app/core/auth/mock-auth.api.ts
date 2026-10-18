import { Injectable } from '@angular/core';
import { delay, of } from 'rxjs';
import type { Observable } from 'rxjs';
import type { AuthApi } from './auth-api';
import type { AuthSession, SignInChallenge, SignInCommand, VerifyMfaCommand } from './auth.models';

const demoCustomer = { id: 'customer-demo-01', firstName: 'Aarav', initials: 'AK', tier: 'Signature Banking' } as const;

@Injectable()
export class MockAuthApi implements AuthApi {
  loginUrl(returnTo: string): string { void returnTo; return '/login'; }
  private authenticated = false;

  getSession(): Observable<AuthSession> {
    return of(this.session()).pipe(delay(150));
  }

  signIn(command: SignInCommand): Observable<SignInChallenge> {
    void command;
    return of<SignInChallenge>({ challengeId: 'fictional-mfa-challenge', method: 'OTP', destinationHint: '•• 42', expiresAt: new Date(Date.now() + 5 * 60_000).toISOString() }).pipe(delay(650));
  }

  verifyMfa(command: VerifyMfaCommand): Observable<AuthSession> {
    this.authenticated = /^\d{6}$/.test(command.code) && command.challengeId === 'fictional-mfa-challenge';
    return of(this.session()).pipe(delay(650));
  }

  continueSession(): Observable<AuthSession> { return of(this.session()).pipe(delay(250)); }
  signOut(): Observable<void> { this.authenticated = false; return of(undefined).pipe(delay(150)); }

  createDemoSession(): AuthSession {
    this.authenticated = true;
    return this.session();
  }

  private session(): AuthSession {
    return this.authenticated
      ? { authenticated: true, customer: demoCustomer, expiresAt: new Date(Date.now() + 15 * 60_000).toISOString() }
      : { authenticated: false };
  }
}
