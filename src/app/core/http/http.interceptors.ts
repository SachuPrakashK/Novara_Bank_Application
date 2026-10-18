import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import type { HttpHeaders, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, tap, throwError } from 'rxjs';
import { NOVARA_CONFIG } from '../config/app-config';
import { SessionService } from '../session/session.service';
import { NovaraApiError } from './api-error';
import { ErrorNotificationService } from './error-notification.service';

const REQUEST_ID_HEADER = 'X-Request-ID';
const CORRELATION_ID_HEADER = 'X-Correlation-ID';

export const bankingContextInterceptor: HttpInterceptorFn = (request, next) => {
  const config = inject(NOVARA_CONFIG);
  if (!isBankingApiRequest(request, config.apiBaseUrl)) return next(request);

  const requestId = crypto.randomUUID();
  return next(request.clone({ withCredentials: true, setHeaders: { [REQUEST_ID_HEADER]: requestId } })).pipe(
    tap((event) => {
      if (event instanceof HttpResponse) {
        // Correlation IDs are deliberately not logged. They remain available on the response for support flows.
        void event.headers.get(CORRELATION_ID_HEADER);
      }
    }),
  );
};

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const config = inject(NOVARA_CONFIG);
  if (!isBankingApiRequest(request, config.apiBaseUrl)) return next(request);
  const session = inject(SessionService);
  const notifications = inject(ErrorNotificationService);

  return next(request).pipe(catchError((error: unknown) => {
    const normalized = normalizeApiError(error);
    if (normalized.kind === 'AUTHENTICATION' && !request.url.includes('/auth/sign-in') && !request.url.includes('/auth/mfa/verify')) {
      session.expire('server');
    } else if (normalized.kind === 'NETWORK' || normalized.kind === 'SERVER') {
      notifications.show(normalized.message, normalized.retryable ? 'Try again' : undefined);
    }
    return throwError(() => normalized);
  }));
};

function isBankingApiRequest(request: HttpRequest<unknown>, apiBaseUrl: string): boolean {
  return request.url === apiBaseUrl || request.url.startsWith(`${apiBaseUrl}/`);
}

export function normalizeApiError(error: unknown): NovaraApiError {
  if (error instanceof NovaraApiError) return error;
  if (!(error instanceof HttpErrorResponse)) return new NovaraApiError('UNKNOWN', 'Something went wrong. Please try again.', 0);

  const correlationId = readHeader(error.headers, CORRELATION_ID_HEADER);
  if (error.status === 0) return new NovaraApiError('NETWORK', 'We could not reach Novara. Check your connection and try again.', 0, correlationId, true);
  if (error.status === 400 || error.status === 422) return new NovaraApiError('VALIDATION', 'Some information needs your attention.', error.status, correlationId, false, readValidationDetails(error.error));
  if (error.status === 401) return new NovaraApiError('AUTHENTICATION', 'Your session is no longer valid. Please sign in again.', 401, correlationId);
  if (error.status === 403) return new NovaraApiError('AUTHORIZATION', 'You do not have permission to perform this action.', 403, correlationId);
  if (error.status === 404) return new NovaraApiError('NOT_FOUND', 'The requested information could not be found.', 404, correlationId);
  if (error.status === 409) return new NovaraApiError('CONFLICT', 'This request conflicts with a more recent change. Refresh and try again.', 409, correlationId);
  if (error.status === 429) return new NovaraApiError('RATE_LIMITED', 'Too many requests. Wait a moment and try again.', 429, correlationId, true);
  if (error.status >= 500) return new NovaraApiError('SERVER', 'Novara is temporarily unavailable. Please try again shortly.', error.status, correlationId, true);
  return new NovaraApiError('UNKNOWN', 'Something went wrong. Please try again.', error.status, correlationId);
}

function readHeader(headers: HttpHeaders, name: string): string | undefined { return headers.get(name) ?? undefined; }
function readValidationDetails(body: unknown): Readonly<Record<string, string>> | undefined {
  if (!body || typeof body !== 'object' || !('errors' in body)) return undefined;
  const errors = (body as { errors?: unknown }).errors;
  if (!errors || typeof errors !== 'object') return undefined;
  return Object.fromEntries(Object.entries(errors).filter((entry): entry is [string, string] => typeof entry[1] === 'string'));
}
