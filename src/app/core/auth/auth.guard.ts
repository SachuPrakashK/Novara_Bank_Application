import { inject } from '@angular/core';
import type { CanActivateFn} from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
export const authGuard: CanActivateFn = () => inject(AuthService).isAuthenticated() ? true : inject(Router).createUrlTree(['/login']);
export const guestGuard: CanActivateFn = () => inject(AuthService).isAuthenticated() ? inject(Router).createUrlTree(['/dashboard']) : true;
export const mfaChallengeGuard: CanActivateFn = () => inject(AuthService).authStatus() === 'mfa-required' ? true : inject(Router).createUrlTree(['/login']);
