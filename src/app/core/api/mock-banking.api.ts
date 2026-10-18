import { Injectable } from '@angular/core';
import type { Observable} from 'rxjs';
import { delay, of } from 'rxjs';
import type { BankAccount, DashboardOverview, Transaction } from '../models/banking.models';
import type { BankingApi } from './banking-api';

const accounts: readonly BankAccount[] = [
  { id: 'acc-savings-01', nickname: 'Everyday Savings', type: 'SAVINGS', maskedNumber: '•••• 4821', balance: { available: { amountMinor: 84265075, currency: 'INR' }, ledger: { amountMinor: 84415075, currency: 'INR' } }, status: 'ACTIVE', detail: '3.5% annual interest' },
  { id: 'acc-current-01', nickname: 'Novara Current', type: 'CURRENT', maskedNumber: '•••• 7394', balance: { available: { amountMinor: 28641020, currency: 'INR' }, ledger: { amountMinor: 28641020, currency: 'INR' } }, status: 'ACTIVE', detail: 'Primary payments account' },
  { id: 'acc-deposit-01', nickname: 'Future Fund', type: 'FIXED_DEPOSIT', maskedNumber: '•••• 1058', balance: { available: { amountMinor: 50000000, currency: 'INR' }, ledger: { amountMinor: 50000000, currency: 'INR' } }, status: 'ACTIVE', detail: 'Matures 18 Mar 2027' },
];
const transactions: readonly Transaction[] = [
  { id: 'txn-001', counterparty: 'Fresh Market', occurredAt: '2026-10-15T08:42:00+05:30', maskedAccount: '•••• 4821', amount: { amountMinor: 248950, currency: 'INR' }, direction: 'DEBIT', status: 'COMPLETED', reference: 'NVR2601019482', category: 'Groceries' },
  { id: 'txn-002', counterparty: 'Acme Technologies', occurredAt: '2026-10-14T17:30:00+05:30', maskedAccount: '•••• 7394', amount: { amountMinor: 18500000, currency: 'INR' }, direction: 'CREDIT', status: 'COMPLETED', reference: 'NVR2601018841', category: 'Salary' },
  { id: 'txn-003', counterparty: 'Urban Electric', occurredAt: '2026-10-13T11:18:00+05:30', maskedAccount: '•••• 4821', amount: { amountMinor: 426780, currency: 'INR' }, direction: 'DEBIT', status: 'PENDING', reference: 'NVR2601017530', category: 'Utilities' },
  { id: 'txn-004', counterparty: 'Indigo Cafe', occurredAt: '2026-10-12T19:06:00+05:30', maskedAccount: '•••• 4821', amount: { amountMinor: 128500, currency: 'INR' }, direction: 'DEBIT', status: 'COMPLETED', reference: 'NVR2601016212', category: 'Dining' },
];

@Injectable()
export class MockBankingApi implements BankingApi {
  getDashboard(): Observable<DashboardOverview> { return of<DashboardOverview>({ customer: { id: 'customer-demo-01', firstName: 'Aarav', initials: 'AK', tier: 'Signature' }, relationshipBalance: { amountMinor: 162906095, currency: 'INR' }, accounts, transactions, monthlySpend: { amountMinor: 6874200, currency: 'INR' }, security: { label: 'Strong', mfaEnabled: true, lastLogin: 'Today, 08:14 · Bengaluru' } }).pipe(delay(450)); }
  getAccounts(): Observable<readonly BankAccount[]> { return of(accounts).pipe(delay(250)); }
  getTransactions(): Observable<readonly Transaction[]> { return of(transactions).pipe(delay(250)); }
}
