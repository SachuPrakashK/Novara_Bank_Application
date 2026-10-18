import type { ApplicationConfig} from '@angular/core';
import { inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { BANKING_API } from './core/api/banking-api';
import { MockBankingApi } from './core/api/mock-banking.api';
import { HttpBankingApi } from './core/api/http-banking.api';
import { NOVARA_CONFIG } from './core/config/app-config';
import { AUTH_API } from './core/auth/auth-api';
import { AuthService } from './core/auth/auth.service';
import { HttpAuthApi } from './core/auth/http-auth.api';
import { MockAuthApi } from './core/auth/mock-auth.api';
import { apiErrorInterceptor, bankingContextInterceptor } from './core/http/http.interceptors';
import { HttpPaymentsApi } from './core/payments/http-payments.api';
import { MockPaymentsApi } from './core/payments/mock-payments.api';
import { PAYMENTS_API } from './core/payments/payments-api';
import { environment } from '../environments/environment';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(
      withFetch(),
      withXsrfConfiguration({ cookieName: 'NOVARA-XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' }),
      withInterceptors([bankingContextInterceptor, apiErrorInterceptor]),
    ),
    provideRouter(routes, withInMemoryScrolling({ scrollPositionRestoration: 'enabled' })),
    { provide: NOVARA_CONFIG, useValue: environment },
    MockBankingApi,
    HttpBankingApi,
    MockAuthApi,
    HttpAuthApi,
    MockPaymentsApi,
    HttpPaymentsApi,
    { provide: BANKING_API, useFactory: () => inject(NOVARA_CONFIG).useMockApi ? inject(MockBankingApi) : inject(HttpBankingApi) },
    { provide: AUTH_API, useFactory: () => inject(NOVARA_CONFIG).useMockApi ? inject(MockAuthApi) : inject(HttpAuthApi) },
    { provide: PAYMENTS_API, useFactory: () => inject(NOVARA_CONFIG).useMockApi ? inject(MockPaymentsApi) : inject(HttpPaymentsApi) },
    provideAppInitializer(() => {
      const config = inject(NOVARA_CONFIG);
      if (config.production && config.useMockApi) throw new Error('Mock banking API must never run in production.');
      return inject(AuthService).initialize();
    }),
  ]
};
