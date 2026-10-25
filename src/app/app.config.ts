import type { ApplicationConfig } from '@angular/core';
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
import { BENEFICIARIES_API } from './core/beneficiaries/beneficiaries-api';
import { HttpBeneficiariesApi } from './core/beneficiaries/http-beneficiaries.api';
import { MockBeneficiariesApi } from './core/beneficiaries/mock-beneficiaries.api';
import { routes } from './app.routes';
import { CARDS_API } from './core/cards/cards-api';
import { HttpCardsApi } from './core/cards/http-cards.api';
import { MockCardsApi } from './core/cards/mock-cards.api';
import { DEPOSITS_API } from './core/deposits/deposits-api';
import { HttpDepositsApi } from './core/deposits/http-deposits.api';
import { MockDepositsApi } from './core/deposits/mock-deposits.api';
import { LOANS_API } from './core/loans/loans-api';
import { HttpLoansApi } from './core/loans/http-loans.api';
import { MockLoansApi } from './core/loans/mock-loans.api';
import { STATEMENTS_API } from './core/statements/statements-api';
import { HttpStatementsApi } from './core/statements/http-statements.api';
import { MockStatementsApi } from './core/statements/mock-statements.api';

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
    HttpBeneficiariesApi,
    MockBeneficiariesApi,
    HttpCardsApi,
    MockCardsApi,
    HttpDepositsApi,
    MockDepositsApi,
    HttpLoansApi,
    MockLoansApi,
    HttpStatementsApi,
    MockStatementsApi,
    {
      provide: BANKING_API,
      useFactory: () =>
        inject(NOVARA_CONFIG).useMockApi ? inject(MockBankingApi) : inject(HttpBankingApi),
    },
    {
      provide: AUTH_API,
      useFactory: () =>
        inject(NOVARA_CONFIG).useMockApi ? inject(MockAuthApi) : inject(HttpAuthApi),
    },
    {
      provide: PAYMENTS_API,
      useFactory: () =>
        inject(NOVARA_CONFIG).useMockApi ? inject(MockPaymentsApi) : inject(HttpPaymentsApi),
    },
    {
      provide: BENEFICIARIES_API,
      useFactory: () =>
        inject(NOVARA_CONFIG).useMockApi
          ? inject(MockBeneficiariesApi)
          : inject(HttpBeneficiariesApi),
    },
    {
      provide: CARDS_API,
      useFactory: () =>
        inject(NOVARA_CONFIG).useMockApi ? inject(MockCardsApi) : inject(HttpCardsApi),
    },
    {
      provide: DEPOSITS_API,
      useFactory: () =>
        inject(NOVARA_CONFIG).useMockApi ? inject(MockDepositsApi) : inject(HttpDepositsApi),
    },
    {
      provide: LOANS_API,
      useFactory: () =>
        inject(NOVARA_CONFIG).useMockApi ? inject(MockLoansApi) : inject(HttpLoansApi),
    },
    {
      provide: STATEMENTS_API,
      useFactory: () => inject(NOVARA_CONFIG).useMockApi ? inject(MockStatementsApi) : inject(HttpStatementsApi),
    },
    provideAppInitializer(() => {
      const config = inject(NOVARA_CONFIG);
      if (config.production && config.useMockApi)
        throw new Error('Mock banking API must never run in production.');
      return inject(AuthService).initialize();
    }),
  ],
};
