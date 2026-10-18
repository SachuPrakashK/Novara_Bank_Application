import { InjectionToken } from '@angular/core';
import type { Observable } from 'rxjs';
import type { BankAccount, DashboardOverview, Transaction } from '../models/banking.models';

export interface BankingApi {
  getDashboard(): Observable<DashboardOverview>;
  getAccounts(): Observable<readonly BankAccount[]>;
  getTransactions(): Observable<readonly Transaction[]>;
}
export const BANKING_API = new InjectionToken<BankingApi>('BANKING_API');
