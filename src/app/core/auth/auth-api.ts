import { InjectionToken } from '@angular/core';
import type { Observable } from 'rxjs';
import type { AuthSession, SignInChallenge, SignInCommand, VerifyMfaCommand } from './auth.models';

export interface AuthApi {
  getSession(): Observable<AuthSession>;
  loginUrl(returnTo: string): string;
  signIn(command: SignInCommand): Observable<SignInChallenge>;
  verifyMfa(command: VerifyMfaCommand): Observable<AuthSession>;
  continueSession(): Observable<AuthSession>;
  signOut(): Observable<void>;
}

export const AUTH_API = new InjectionToken<AuthApi>('AUTH_API');
