import { inject } from '@angular/core';import type { CanActivateFn } from '@angular/router';import { Router } from '@angular/router';import { NOVARA_CONFIG } from '../config/app-config';
export const operationsDemoGuard:CanActivateFn=()=>{const c=inject(NOVARA_CONFIG);return !c.production&&c.useMockApi?true:inject(Router).createUrlTree(['/login']);};
