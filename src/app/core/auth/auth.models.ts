export type AuthStatus = 'initializing' | 'anonymous' | 'mfa-required' | 'authenticated' | 'expired';

export interface AuthenticatedCustomer {
  readonly id: string;
  readonly firstName: string;
  readonly initials: string;
  readonly tier: string;
}

export interface AuthSession {
  readonly authenticated: boolean;
  readonly customer?: AuthenticatedCustomer;
  readonly expiresAt?: string;
}

export interface SignInCommand {
  readonly username: string;
  readonly password: string;
  readonly rememberDevice: boolean;
}

export interface SignInChallenge {
  readonly challengeId: string;
  readonly method: 'OTP' | 'PASSKEY';
  readonly destinationHint?: string;
  readonly expiresAt: string;
}

export interface VerifyMfaCommand {
  readonly challengeId: string;
  readonly code: string;
}
