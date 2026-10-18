export type Currency = 'INR';
export interface Money { readonly amountMinor: number; readonly currency: Currency; }
export type AccountStatus = 'ACTIVE' | 'DORMANT' | 'RESTRICTED';
export interface AccountBalance { readonly available: Money; readonly ledger: Money; }
export interface BankAccount { readonly id: string; readonly nickname: string; readonly type: 'SAVINGS' | 'CURRENT' | 'FIXED_DEPOSIT'; readonly maskedNumber: string; readonly balance: AccountBalance; readonly status: AccountStatus; readonly detail: string; }
export type TransactionStatus = 'COMPLETED' | 'PENDING' | 'FAILED' | 'REVERSED';
export interface Transaction { readonly id: string; readonly counterparty: string; readonly occurredAt: string; readonly maskedAccount: string; readonly amount: Money; readonly direction: 'DEBIT' | 'CREDIT'; readonly status: TransactionStatus; readonly reference: string; readonly category: string; readonly note?: string; }
export interface CustomerProfile { readonly id: string; readonly firstName: string; readonly initials: string; readonly tier: string; }
export interface DashboardOverview { readonly customer: CustomerProfile; readonly relationshipBalance: Money; readonly accounts: readonly BankAccount[]; readonly transactions: readonly Transaction[]; readonly monthlySpend: Money; readonly security: { readonly label: string; readonly mfaEnabled: boolean; readonly lastLogin: string }; }
export interface Beneficiary { readonly id: string; readonly name: string; readonly maskedAccount: string; readonly favorite: boolean; }
export interface Payment { readonly id: string; readonly idempotencyKey: string; readonly status: 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'UNKNOWN'; }
export type PaymentMethod = 'INTERNAL' | 'UPI' | 'IMPS' | 'NEFT' | 'RTGS';
export interface Card { readonly id: string; readonly maskedNumber: string; readonly status: 'ACTIVE' | 'FROZEN'; }
export interface Deposit { readonly id: string; readonly principal: Money; readonly status: string; }
export interface Loan { readonly id: string; readonly outstanding: Money; readonly status: string; }
export interface Statement { readonly id: string; readonly periodLabel: string; readonly status: 'AVAILABLE' | 'PROCESSING'; }
export interface BankNotification { readonly id: string; readonly title: string; readonly read: boolean; }
export interface SecuritySession { readonly id: string; readonly current: boolean; readonly lastActiveAt: string; }
export interface Device { readonly id: string; readonly label: string; readonly trusted: boolean; }
