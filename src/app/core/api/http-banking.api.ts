import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, type Observable } from 'rxjs';
import { NOVARA_CONFIG } from '../config/app-config';
import type { BankAccount, DashboardOverview, Transaction } from '../models/banking.models';
import type { BankingApi } from './banking-api';

@Injectable()
export class HttpBankingApi implements BankingApi {
  private readonly http = inject(HttpClient);
  private readonly config = inject(NOVARA_CONFIG);
  getDashboard(): Observable<DashboardOverview> {
    return this.http.get<DashboardResponse>(`${this.config.apiBaseUrl}/customers/me/dashboard`).pipe(map((dashboard) => ({ ...dashboard, accounts: dashboard.accounts.map(toBankAccount) })));
  }
  getAccounts(): Observable<readonly BankAccount[]> {
    return this.http.get<readonly AccountResponse[]>(`${this.config.apiBaseUrl}/accounts`).pipe(map((accounts) => accounts.map(toBankAccount)));
  }
  getTransactions(): Observable<readonly Transaction[]> { return this.http.get<readonly Transaction[]>(`${this.config.apiBaseUrl}/transactions`); }
}

interface DashboardResponse extends Omit<DashboardOverview, 'accounts'> { readonly accounts: readonly AccountResponse[]; }

interface AccountResponse {
  readonly id: string;
  readonly nickname: string;
  readonly type: BankAccount['type'] | 'SETTLEMENT';
  readonly maskedNumber: string;
  readonly currency: 'INR';
  readonly status: BankAccount['status'];
  readonly availableBalanceMinor: number;
  readonly ledgerBalanceMinor: number;
}

function toBankAccount(account: AccountResponse): BankAccount {
  const type: BankAccount['type'] = account.type === 'SETTLEMENT' ? 'CURRENT' : account.type;
  return {
    id: account.id,
    nickname: account.nickname,
    type,
    maskedNumber: account.maskedNumber,
    status: account.status,
    detail: `${type.replace('_', ' ')} account`,
    balance: {
      available: { amountMinor: account.availableBalanceMinor, currency: account.currency },
      ledger: { amountMinor: account.ledgerBalanceMinor, currency: account.currency },
    },
  };
}
