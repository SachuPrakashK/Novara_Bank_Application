export type ApiErrorKind = 'NETWORK' | 'TIMEOUT' | 'VALIDATION' | 'AUTHENTICATION' | 'AUTHORIZATION' | 'NOT_FOUND' | 'CONFLICT' | 'RATE_LIMITED' | 'SERVER' | 'UNKNOWN';

export class NovaraApiError extends Error {
  constructor(
    readonly kind: ApiErrorKind,
    message: string,
    readonly status: number,
    readonly correlationId?: string,
    readonly retryable = false,
    readonly details?: Readonly<Record<string, string>>,
  ) {
    super(message);
    this.name = 'NovaraApiError';
  }
}
