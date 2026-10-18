import { InjectionToken } from '@angular/core';
export interface NovaraConfig {
  readonly apiBaseUrl: string;
  readonly useMockApi: boolean;
  readonly production: boolean;
  readonly session: { readonly idleTimeoutMs: number; readonly warningDurationMs: number };
}
export const NOVARA_CONFIG = new InjectionToken<NovaraConfig>('NOVARA_CONFIG');
